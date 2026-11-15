package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import com.google.cloud.firestore.DocumentReference;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.TransactionMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.TransactionRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletSwapRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.request.WalletTransferRequest;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionImportResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.TransactionResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletSwapResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletTransferResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import dev.guilhermealves.assets.portfolio.api.app.domain.utils.CsvUtils;
import dev.guilhermealves.assets.portfolio.api.app.exception.BusinessException;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionCore {

    private final TransactionMapper transactionMapper;
    private final TransactionFireBaseAdapter transactionFireBaseAdapter;
    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final AssetCore assetCore;
    private final WalletCore walletCore;
    private final SecurityCore securityCore;
    private final PortfolioAnalyticsCore portfolioAnalyticsCore;

    public TransactionResponse create(TransactionRequest transaction) throws Exception {
        // Valida se a carteira existe e pertence ao usuário autenticado
        WalletDocument walletDoc = walletFireBaseAdapter.findById(transaction.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", transaction.getWalletId()));
        walletCore.validateWalletOwnership(walletDoc);

        // Valida e resolve (ou cria automaticamente) o ativo
        AssetDocument assetDoc = assetCore.findOrCreateAsset(
                transaction.getEffectiveAssetIdentifier(),
                transaction.getAssetType(),
                transaction.getAssetName()
        );
        String assetId = assetDoc.getId();
        transaction.setAssetId(assetId);

        TransactionDocument docRequest = transactionMapper.map(transaction);

        if (docRequest.getCurrency() == null || docRequest.getCurrency().isBlank()) {
            if ("FII".equalsIgnoreCase(assetDoc.getType()) || "STOCK_BR".equalsIgnoreCase(assetDoc.getType())) {
                docRequest.setCurrency("BRL");
            } else if (assetDoc.getSymbol() != null && assetDoc.getSymbol().matches("^[A-Z]{4}[0-9]{1,2}[A-Z]?$")) {
                docRequest.setCurrency("BRL");
            } else {
                docRequest.setCurrency("USD");
            }
        }

        DocumentReference wallet = walletFireBaseAdapter.getDocReference(transaction.getWalletId());
        DocumentReference asset = assetFireBaseAdapter.getDocReference(assetId);

        docRequest.setWallet(wallet);
        docRequest.setAsset(asset);
        docRequest.setWalletId(transaction.getWalletId());
        docRequest.setAssetId(assetId);

        TransactionDocument doc = transactionFireBaseAdapter.create(docRequest);
        return transactionMapper.map(doc);
    }

    public TransactionResponse update(TransactionRequest transaction) throws Exception {
        TransactionDocument existingTx = transactionFireBaseAdapter.findById(transaction.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação", transaction.getId()));
        validateTransactionOwnership(existingTx);

        if (transaction.getWalletId() != null) {
            WalletDocument targetWallet = walletFireBaseAdapter.findById(transaction.getWalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", transaction.getWalletId()));
            walletCore.validateWalletOwnership(targetWallet);
        }

        String aId = existingTx.getAssetId();
        String assetIdentifier = transaction.getEffectiveAssetIdentifier() != null
                ? transaction.getEffectiveAssetIdentifier()
                : aId;

        if (assetIdentifier != null) {
            AssetDocument assetDoc = assetCore.findOrCreateAsset(
                    assetIdentifier,
                    transaction.getAssetType(),
                    transaction.getAssetName()
            );
            aId = assetDoc.getId();
            transaction.setAssetId(aId);
        }

        TransactionDocument docRequest = transactionMapper.map(transaction);

        if (docRequest.getCurrency() == null || docRequest.getCurrency().isBlank()) {
            if (existingTx.getCurrency() != null && !existingTx.getCurrency().isBlank()) {
                docRequest.setCurrency(existingTx.getCurrency());
            } else if (transaction.getAssetType() == AssetType.FII || transaction.getAssetType() == AssetType.STOCK_BR) {
                docRequest.setCurrency("BRL");
            } else {
                docRequest.setCurrency("USD");
            }
        }

        String wId = transaction.getWalletId() != null ? transaction.getWalletId() : existingTx.getWalletId();

        DocumentReference wallet = walletFireBaseAdapter.getDocReference(wId);
        DocumentReference asset = assetFireBaseAdapter.getDocReference(aId);

        docRequest.setWallet(wallet);
        docRequest.setAsset(asset);
        docRequest.setWalletId(wId);
        docRequest.setAssetId(aId);

        TransactionDocument doc = transactionFireBaseAdapter.update(docRequest);
        return transactionMapper.map(doc);
    }

    public List<TransactionResponse> findAll() throws Exception {
        Set<String> userWalletIds = getUserWalletIds();
        List<TransactionDocument> allTxs = transactionFireBaseAdapter.findAll();

        List<TransactionDocument> userTxs = allTxs.stream()
                .filter(tx -> isTxInWallets(tx, userWalletIds))
                .collect(Collectors.toList());

        return transactionMapper.mapList(userTxs);
    }

    public List<TransactionResponse> findByFilter(String walletId, String assetId, Integer page, Integer size) throws Exception {
        Set<String> userWalletIds = getUserWalletIds();

        if (walletId != null && !walletId.isBlank()) {
            WalletDocument walletDoc = walletFireBaseAdapter.findById(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", walletId));
            walletCore.validateWalletOwnership(walletDoc);
        }

        List<TransactionDocument> transactionDocs = transactionFireBaseAdapter.findWithFilter(walletId, assetId, page, size);
        List<TransactionDocument> userTxs = transactionDocs.stream()
                .filter(tx -> isTxInWallets(tx, userWalletIds))
                .collect(Collectors.toList());

        return transactionMapper.mapList(userTxs);
    }

    public Optional<TransactionResponse> findById(String id) throws Exception {
        Optional<TransactionDocument> optionalDoc = transactionFireBaseAdapter.findById(id);

        if (optionalDoc.isPresent()) {
            validateTransactionOwnership(optionalDoc.get());
            return Optional.of(transactionMapper.map(optionalDoc.get()));
        }

        return Optional.empty();
    }

    public void deleteById(String id) throws Exception {
        TransactionDocument existingTx = transactionFireBaseAdapter.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
        validateTransactionOwnership(existingTx);

        transactionFireBaseAdapter.deleteById(id);
    }

    private void validateTransactionOwnership(TransactionDocument tx) throws Exception {
        String wId = tx.getWalletId() != null ? tx.getWalletId() :
                (tx.getWallet() != null ? tx.getWallet().getId() : null);

        if (wId != null) {
            WalletDocument wallet = walletFireBaseAdapter.findById(wId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", wId));
            walletCore.validateWalletOwnership(wallet);
        }
    }

    private Set<String> getUserWalletIds() throws Exception {
        String currentUserId = securityCore.getCurrentUserId();
        return walletFireBaseAdapter.findAll().stream()
                .filter(w -> w.getUser() != null && currentUserId.equals(w.getUser().getId()))
                .map(WalletDocument::getId)
                .collect(Collectors.toSet());
    }

    private boolean isTxInWallets(TransactionDocument tx, Set<String> walletIds) {
        String wId = tx.getWalletId() != null ? tx.getWalletId() :
                (tx.getWallet() != null ? tx.getWallet().getId() : null);
        return wId != null && walletIds.contains(wId);
    }

    public byte[] exportTransactionsToCsv(String walletId) throws Exception {
        Set<String> userWalletIds = getUserWalletIds();

        if (walletId != null && !walletId.isBlank()) {
            WalletDocument walletDoc = walletFireBaseAdapter.findById(walletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira", walletId));
            walletCore.validateWalletOwnership(walletDoc);
        }

        List<TransactionDocument> allTxs = transactionFireBaseAdapter.findAll();
        List<TransactionDocument> targetTxs = allTxs.stream()
                .filter(tx -> isTxInWallets(tx, userWalletIds))
                .filter(tx -> {
                    if (walletId == null || walletId.isBlank()) return true;
                    String wId = tx.getWalletId() != null ? tx.getWalletId() :
                            (tx.getWallet() != null ? tx.getWallet().getId() : null);
                    return walletId.equals(wId);
                })
                .sorted(Comparator.comparing((TransactionDocument tx) -> tx.getDatetime() != null ? tx.getDatetime().toDate() : new Date(0)))
                .collect(Collectors.toList());

        Map<String, AssetDocument> assetMap = new HashMap<>();
        for (AssetDocument a : assetFireBaseAdapter.findAll()) {
            assetMap.put(a.getId(), a);
        }

        Map<String, WalletDocument> walletMap = new HashMap<>();
        for (WalletDocument w : walletFireBaseAdapter.findAll()) {
            walletMap.put(w.getId(), w);
        }

        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF"); // UTF-8 BOM
        csv.append("id,datetime,type,walletId,walletName,assetSymbol,assetName,assetType,quantity,price,amount,fee\n");

        for (TransactionDocument tx : targetTxs) {
            String wId = tx.getWalletId() != null ? tx.getWalletId() : (tx.getWallet() != null ? tx.getWallet().getId() : "");
            WalletDocument wDoc = walletMap.get(wId);
            String wName = (wDoc != null) ? wDoc.getName() : "";

            String aId = tx.getAssetId() != null ? tx.getAssetId() : (tx.getAsset() != null ? tx.getAsset().getId() : "");
            AssetDocument aDoc = assetMap.get(aId);
            String aSymbol = (aDoc != null) ? aDoc.getSymbol() : "";
            String aName = (aDoc != null) ? aDoc.getName() : "";
            String aType = (aDoc != null && aDoc.getType() != null) ? aDoc.getType() : "";

            String dtStr = "";
            if (tx.getDatetime() != null) {
                dtStr = tx.getDatetime().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
            }

            csv.append(CsvUtils.escapeCsv(tx.getId())).append(",");
            csv.append(CsvUtils.escapeCsv(dtStr)).append(",");
            csv.append(CsvUtils.escapeCsv(tx.getType())).append(",");
            csv.append(CsvUtils.escapeCsv(wId)).append(",");
            csv.append(CsvUtils.escapeCsv(wName)).append(",");
            csv.append(CsvUtils.escapeCsv(aSymbol)).append(",");
            csv.append(CsvUtils.escapeCsv(aName)).append(",");
            csv.append(CsvUtils.escapeCsv(aType)).append(",");
            csv.append(tx.getQuantity() != null ? tx.getQuantity().toPlainString() : "0").append(",");
            csv.append(tx.getPrice() != null ? tx.getPrice().toPlainString() : "0").append(",");
            csv.append(tx.getAmount() != null ? tx.getAmount().toPlainString() : "0").append(",");
            csv.append(tx.getFee() != null ? tx.getFee().toPlainString() : "0").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public TransactionImportResponse importTransactionsFromCsv(InputStream inputStream, String defaultWalletId) throws Exception {
        List<Map<String, String>> records = CsvUtils.parseCsv(inputStream);
        TransactionImportResponse response = new TransactionImportResponse();
        response.setTotalProcessed(records.size());

        if (records.isEmpty()) {
            return response;
        }

        Set<String> userWalletIds = getUserWalletIds();

        if (defaultWalletId != null && !defaultWalletId.isBlank()) {
            if (!userWalletIds.contains(defaultWalletId)) {
                WalletDocument w = walletFireBaseAdapter.findById(defaultWalletId)
                        .orElseThrow(() -> new ResourceNotFoundException("Carteira", defaultWalletId));
                walletCore.validateWalletOwnership(w);
            }
        }

        int rowNum = 1;
        for (Map<String, String> row : records) {
            rowNum++;
            try {
                String walletId = getFirstNonBlank(row, "walletid", "carteiraid", "wallet", "carteira");
                if (walletId == null || walletId.isBlank()) {
                    walletId = defaultWalletId;
                }
                if (walletId == null || walletId.isBlank()) {
                    throw new IllegalArgumentException("Carteira não especificada");
                }
                if (!userWalletIds.contains(walletId)) {
                    throw new AccessDeniedException("Carteira " + walletId + " não pertence ao usuário");
                }

                String assetSymbol = getFirstNonBlank(row, "assetsymbol", "symbol", "simbolo", "ticker", "asset", "ativo");
                String assetId = getFirstNonBlank(row, "assetid", "idativo");
                if ((assetSymbol == null || assetSymbol.isBlank()) && (assetId == null || assetId.isBlank())) {
                    throw new IllegalArgumentException("Símbolo ou ID do ativo não informado");
                }

                String typeStr = getFirstNonBlank(row, "type", "tipo");
                TransactionType type = TransactionType.BUY;
                if (typeStr != null && !typeStr.isBlank()) {
                    type = TransactionType.fromString(typeStr);
                }

                BigDecimal quantity = parseBigDecimal(getFirstNonBlank(row, "quantity", "quantidade", "qtd", "shares"), BigDecimal.ZERO);
                BigDecimal price = parseBigDecimal(getFirstNonBlank(row, "price", "preco", "precodecompra", "cotacao"), BigDecimal.ZERO);
                BigDecimal fee = parseBigDecimal(getFirstNonBlank(row, "fee", "taxa", "custo", "fees"), BigDecimal.ZERO);
                BigDecimal amount = parseBigDecimal(getFirstNonBlank(row, "amount", "valor", "valortotal", "total"), null);

                if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                    amount = price.multiply(quantity);
                }
                if (amount.compareTo(BigDecimal.ZERO) <= 0 && type != TransactionType.AIRDROP && type != TransactionType.REWARD && type != TransactionType.STAKING) {
                    amount = BigDecimal.valueOf(0.01);
                }

                String datetimeStr = getFirstNonBlank(row, "datetime", "data", "date", "timestamp");
                if (datetimeStr == null || datetimeStr.isBlank()) {
                    datetimeStr = LocalDateTime.now().toString();
                } else if (!datetimeStr.contains("T") && datetimeStr.length() == 10) {
                    datetimeStr = datetimeStr + "T00:00:00";
                }

                TransactionRequest req = new TransactionRequest();
                req.setType(type);
                req.setWalletId(walletId);
                req.setAssetId(assetId);
                req.setAssetSymbol(assetSymbol);
                req.setQuantity(quantity);
                req.setPrice(price);
                req.setAmount(amount);
                req.setFee(fee);
                req.setDatetime(datetimeStr);

                TransactionResponse created = create(req);
                response.getImportedTransactions().add(created);
                response.setTotalSuccess(response.getTotalSuccess() + 1);

            } catch (Exception e) {
                response.setTotalFailed(response.getTotalFailed() + 1);
                response.getErrors().add("Linha " + rowNum + ": " + e.getMessage());
                log.warn("Erro ao importar linha {} do CSV: {}", rowNum, e.getMessage());
            }
        }

        return response;
    }

    public WalletTransferResponse transferBetweenWallets(WalletTransferRequest request) throws Exception {
        if (request.getSourceWalletId().equals(request.getTargetWalletId())) {
            throw new BusinessException("A carteira de origem e a carteira de destino não podem ser iguais");
        }

        WalletDocument sourceWallet = walletFireBaseAdapter.findById(request.getSourceWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira de origem", request.getSourceWalletId()));
        walletCore.validateWalletOwnership(sourceWallet);

        WalletDocument targetWallet = walletFireBaseAdapter.findById(request.getTargetWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira de destino", request.getTargetWalletId()));
        walletCore.validateWalletOwnership(targetWallet);

        AssetDocument assetDoc = assetCore.findOrCreateAsset(
                request.getEffectiveAssetIdentifier(),
                null,
                null
        );

        List<HoldingResponse> sourceHoldings = portfolioAnalyticsCore.calculateWalletHoldings(request.getSourceWalletId(), null, "USD");
        HoldingResponse holding = sourceHoldings.stream()
                .filter(h -> h.getAsset() != null &&
                        (assetDoc.getId().equals(h.getAsset().getId()) || assetDoc.getSymbol().equalsIgnoreCase(h.getAsset().getSymbol())))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Ativo " + assetDoc.getSymbol() + " não encontrado em custódia na carteira de origem"));

        if (holding.getQuantity().compareTo(request.getQuantity()) < 0) {
            throw new BusinessException("Saldo insuficiente na carteira de origem. Saldo disponível: " +
                    holding.getQuantity() + " " + assetDoc.getSymbol() + ", solicitado: " + request.getQuantity());
        }

        BigDecimal transferPrice = holding.getAvgBuyPrice();
        if (transferPrice == null || transferPrice.compareTo(BigDecimal.ZERO) <= 0) {
            transferPrice = (holding.getCurrentPrice() != null) ? holding.getCurrentPrice() : BigDecimal.ONE;
        }

        BigDecimal totalAmount = transferPrice.multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = (request.getFee() != null) ? request.getFee() : BigDecimal.ZERO;

        String datetime = (request.getDatetime() != null && !request.getDatetime().isBlank())
                ? request.getDatetime()
                : LocalDateTime.now().toString();

        TransactionRequest outReq = new TransactionRequest();
        outReq.setType(TransactionType.TRANSFER_OUT);
        outReq.setWalletId(request.getSourceWalletId());
        outReq.setAssetId(assetDoc.getId());
        outReq.setQuantity(request.getQuantity());
        outReq.setPrice(transferPrice);
        outReq.setAmount(totalAmount);
        outReq.setFee(fee);
        outReq.setDatetime(datetime);

        TransactionResponse outTx = create(outReq);

        TransactionRequest inReq = new TransactionRequest();
        inReq.setType(TransactionType.TRANSFER_IN);
        inReq.setWalletId(request.getTargetWalletId());
        inReq.setAssetId(assetDoc.getId());
        inReq.setQuantity(request.getQuantity());
        inReq.setPrice(transferPrice);
        inReq.setAmount(totalAmount);
        inReq.setFee(BigDecimal.ZERO);
        inReq.setDatetime(datetime);

        TransactionResponse inTx = create(inReq);

        return WalletTransferResponse.builder()
                .sourceTransaction(outTx)
                .targetTransaction(inTx)
                .sourceWalletId(sourceWallet.getId())
                .sourceWalletName(sourceWallet.getName())
                .targetWalletId(targetWallet.getId())
                .targetWalletName(targetWallet.getName())
                .assetSymbol(assetDoc.getSymbol())
                .quantity(request.getQuantity())
                .transferPrice(transferPrice)
                .totalAmount(totalAmount)
                .fee(fee)
                .build();
    }

    public WalletSwapResponse swapInWallet(String walletId, WalletSwapRequest request) throws Exception {
        String effectiveWalletId = (walletId != null && !walletId.isBlank()) ? walletId : request.getWalletId();
        if (effectiveWalletId == null || effectiveWalletId.isBlank()) {
            throw new IllegalArgumentException("O ID da carteira é obrigatório");
        }

        WalletDocument walletDoc = walletFireBaseAdapter.findById(effectiveWalletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", effectiveWalletId));
        walletCore.validateWalletOwnership(walletDoc);

        AssetDocument fromAssetDoc = assetCore.findOrCreateAsset(request.getFromAsset(), null, null);
        AssetDocument toAssetDoc = assetCore.findOrCreateAsset(request.getToAsset(), null, null);

        List<HoldingResponse> holdings = portfolioAnalyticsCore.calculateWalletHoldings(effectiveWalletId, null, "USD");
        HoldingResponse fromHolding = holdings.stream()
                .filter(h -> h.getAsset() != null &&
                        (fromAssetDoc.getId().equals(h.getAsset().getId()) || fromAssetDoc.getSymbol().equalsIgnoreCase(h.getAsset().getSymbol())))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Ativo de origem " + fromAssetDoc.getSymbol() + " não encontrado em custódia na carteira"));

        if (fromHolding.getQuantity().compareTo(request.getFromQuantity()) < 0) {
            throw new BusinessException("Saldo insuficiente de " + fromAssetDoc.getSymbol() + ". Disponível: " +
                    fromHolding.getQuantity() + ", solicitado: " + request.getFromQuantity());
        }

        BigDecimal fee = (request.getFee() != null) ? request.getFee() : BigDecimal.ZERO;
        String datetime = (request.getDatetime() != null && !request.getDatetime().isBlank())
                ? request.getDatetime()
                : LocalDateTime.now().toString();

        BigDecimal fromPrice = (fromHolding.getCurrentPrice() != null && fromHolding.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0)
                ? fromHolding.getCurrentPrice()
                : fromHolding.getAvgBuyPrice();

        BigDecimal fromAmount = fromPrice.multiply(request.getFromQuantity()).setScale(2, RoundingMode.HALF_UP);

        BigDecimal costBasis = fromHolding.getAvgBuyPrice().multiply(request.getFromQuantity());
        BigDecimal realizedPnL = fromAmount.subtract(fee).subtract(costBasis).setScale(2, RoundingMode.HALF_UP);

        TransactionRequest sellReq = new TransactionRequest();
        sellReq.setType(TransactionType.SELL);
        sellReq.setWalletId(effectiveWalletId);
        sellReq.setAssetId(fromAssetDoc.getId());
        sellReq.setQuantity(request.getFromQuantity());
        sellReq.setPrice(fromPrice);
        sellReq.setAmount(fromAmount);
        sellReq.setFee(fee);
        sellReq.setDatetime(datetime);

        TransactionResponse sellTx = create(sellReq);

        BigDecimal toPrice = fromAmount.divide(request.getToQuantity(), 8, RoundingMode.HALF_UP);
        TransactionRequest buyReq = new TransactionRequest();
        buyReq.setType(TransactionType.BUY);
        buyReq.setWalletId(effectiveWalletId);
        buyReq.setAssetId(toAssetDoc.getId());
        buyReq.setQuantity(request.getToQuantity());
        buyReq.setPrice(toPrice);
        buyReq.setAmount(fromAmount);
        buyReq.setFee(BigDecimal.ZERO);
        buyReq.setDatetime(datetime);

        TransactionResponse buyTx = create(buyReq);

        return WalletSwapResponse.builder()
                .walletId(effectiveWalletId)
                .walletName(walletDoc.getName())
                .fromSymbol(fromAssetDoc.getSymbol())
                .fromQuantity(request.getFromQuantity())
                .toSymbol(toAssetDoc.getSymbol())
                .toQuantity(request.getToQuantity())
                .realizedProfitLoss(realizedPnL)
                .fromTransaction(sellTx)
                .toTransaction(buyTx)
                .message("Troca (SWAP) de " + request.getFromQuantity() + " " + fromAssetDoc.getSymbol() +
                        " por " + request.getToQuantity() + " " + toAssetDoc.getSymbol() + " realizada com sucesso")
                .build();
    }

    private String getFirstNonBlank(Map<String, String> row, String... keys) {
        for (String k : keys) {
            String val = row.get(k);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String val, BigDecimal defaultVal) {
        if (val == null || val.isBlank()) {
            return defaultVal;
        }
        try {
            return new BigDecimal(val.trim().replace(",", "."));
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
