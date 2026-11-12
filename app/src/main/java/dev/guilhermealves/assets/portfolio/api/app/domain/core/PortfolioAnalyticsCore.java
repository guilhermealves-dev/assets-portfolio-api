package dev.guilhermealves.assets.portfolio.api.app.domain.core;

import dev.guilhermealves.assets.portfolio.api.app.adapters.out.AssetFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.TransactionFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.adapters.out.WalletFireBaseAdapter;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.AssetDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.TransactionDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.entity.WalletDocument;
import dev.guilhermealves.assets.portfolio.api.app.domain.mapper.AssetMapper;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.AssetResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.HoldingResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.PortfolioSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.api.response.WalletSummaryResponse;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.AssetType;
import dev.guilhermealves.assets.portfolio.api.app.domain.model.enums.TransactionType;
import dev.guilhermealves.assets.portfolio.api.app.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioAnalyticsCore {

    private final WalletFireBaseAdapter walletFireBaseAdapter;
    private final TransactionFireBaseAdapter transactionFireBaseAdapter;
    private final AssetFireBaseAdapter assetFireBaseAdapter;
    private final AssetMapper assetMapper;
    private final MarketDataService marketDataService;
    private final SecurityCore securityCore;
    private final WalletCore walletCore;

    /**
     * Calcula as posições individuais (Holdings) de uma carteira específica em USD.
     */
    public List<HoldingResponse> calculateWalletHoldings(String walletId, Map<String, BigDecimal> currentPrices) throws Exception {
        return calculateWalletHoldings(walletId, currentPrices, "USD");
    }

    /**
     * Calcula as posições individuais (Holdings) de uma carteira na moeda especificada (USD ou BRL).
     */
    public List<HoldingResponse> calculateWalletHoldings(String walletId, Map<String, BigDecimal> currentPrices, String currency) throws Exception {
        WalletDocument wallet = walletFireBaseAdapter.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", walletId));
        walletCore.validateWalletOwnership(wallet);

        List<TransactionDocument> transactions = transactionFireBaseAdapter.findByWalletId(walletId);

        if (transactions.isEmpty()) {
            transactions = transactionFireBaseAdapter.findAll().stream()
                    .filter(tx -> isTransactionOfWallet(tx, walletId))
                    .collect(Collectors.toList());
        }

        return computeHoldingsFromTransactions(transactions, currentPrices, currency);
    }

    /**
     * Calcula o resumo analítico e financeiro consolidado de uma carteira em USD.
     */
    public WalletSummaryResponse calculateWalletSummary(String walletId, Map<String, BigDecimal> currentPrices) throws Exception {
        return calculateWalletSummary(walletId, currentPrices, "USD");
    }

    /**
     * Calcula o resumo analítico e financeiro consolidado de uma carteira na moeda especificada (USD ou BRL).
     */
    public WalletSummaryResponse calculateWalletSummary(String walletId, Map<String, BigDecimal> currentPrices, String currency) throws Exception {
        WalletDocument wallet = walletFireBaseAdapter.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira", walletId));

        String targetCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
        List<HoldingResponse> holdings = calculateWalletHoldings(walletId, currentPrices, targetCurrency);

        List<TransactionDocument> transactions = transactionFireBaseAdapter.findByWalletId(walletId);
        if (transactions.isEmpty()) {
            transactions = transactionFireBaseAdapter.findAll().stream()
                    .filter(tx -> isTransactionOfWallet(tx, walletId))
                    .collect(Collectors.toList());
        }

        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal realizedPnL = BigDecimal.ZERO;

        Map<AssetType, BigDecimal> allocationByType = new HashMap<>();

        for (HoldingResponse holding : holdings) {
            totalBalance = totalBalance.add(holding.getTotalValue());
            totalInvested = totalInvested.add(holding.getTotalInvested());
            realizedPnL = realizedPnL.add(holding.getRealizedProfitLoss());

            AssetType type = holding.getAsset() != null ? holding.getAsset().getType() : AssetType.CRYPTO;
            BigDecimal currentTypeTotal = allocationByType.getOrDefault(type, BigDecimal.ZERO);
            allocationByType.put(type, currentTypeTotal.add(holding.getTotalValue()));
        }

        if (totalBalance.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<AssetType, BigDecimal> entry : allocationByType.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalBalance, 2, RoundingMode.HALF_UP);
                entry.setValue(percentage);
            }
        }

        BigDecimal unrealizedPnL = totalBalance.subtract(totalInvested).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPnLPercentage = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            unrealizedPnLPercentage = unrealizedPnL.multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        BigDecimal allTimePnL = unrealizedPnL.add(realizedPnL).setScale(2, RoundingMode.HALF_UP);
        BigDecimal allTimePnLPercentage = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            allTimePnLPercentage = allTimePnL.multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        return WalletSummaryResponse.builder()
                .walletId(wallet.getId())
                .walletName(wallet.getName())
                .currency(targetCurrency)
                .totalBalance(totalBalance.setScale(2, RoundingMode.HALF_UP))
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .unrealizedProfitLoss(unrealizedPnL)
                .unrealizedProfitLossPercentage(unrealizedPnLPercentage)
                .realizedProfitLoss(realizedPnL.setScale(2, RoundingMode.HALF_UP))
                .allTimeProfitLoss(allTimePnL)
                .allTimeProfitLossPercentage(allTimePnLPercentage)
                .totalAssetsCount((int) holdings.stream().filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0).count())
                .totalTransactionsCount(transactions.size())
                .allocationByType(allocationByType)
                .build();
    }

    /**
     * Calcula o resumo geral somando todas as carteiras de um usuário em USD.
     */
    public PortfolioSummaryResponse calculatePortfolioSummary(String userId, Map<String, BigDecimal> currentPrices) throws Exception {
        return calculatePortfolioSummary(userId, currentPrices, "USD");
    }

    /**
     * Calcula o resumo geral somando todas as carteiras de um usuário na moeda especificada.
     */
    public PortfolioSummaryResponse calculatePortfolioSummary(String userId, Map<String, BigDecimal> currentPrices, String currency) throws Exception {
        String targetUserId = (userId != null && !userId.isBlank()) ? userId : securityCore.getCurrentUserId();
        if (!targetUserId.equals(securityCore.getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado: você só pode consultar o resumo do seu próprio portfólio");
        }

        String targetCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
        List<WalletDocument> allWallets = walletFireBaseAdapter.findAll();

        List<WalletDocument> userWallets = allWallets.stream()
                .filter(w -> isWalletOfUser(w, targetUserId))
                .collect(Collectors.toList());

        List<WalletSummaryResponse> walletSummaries = new ArrayList<>();
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal realizedPnL = BigDecimal.ZERO;

        for (WalletDocument w : userWallets) {
            WalletSummaryResponse summary = calculateWalletSummary(w.getId(), currentPrices, targetCurrency);
            walletSummaries.add(summary);

            totalBalance = totalBalance.add(summary.getTotalBalance());
            totalInvested = totalInvested.add(summary.getTotalInvested());
            realizedPnL = realizedPnL.add(summary.getRealizedProfitLoss());
        }

        BigDecimal unrealizedPnL = totalBalance.subtract(totalInvested).setScale(2, RoundingMode.HALF_UP);
        BigDecimal unrealizedPnLPercentage = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            unrealizedPnLPercentage = unrealizedPnL.multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        BigDecimal allTimePnL = unrealizedPnL.add(realizedPnL).setScale(2, RoundingMode.HALF_UP);

        // Agregação de alocação global
        List<HoldingResponse> consolidatedHoldings = calculatePortfolioHoldings(userId, currentPrices, targetCurrency);
        Map<AssetType, BigDecimal> allocationByType = new HashMap<>();

        for (HoldingResponse holding : consolidatedHoldings) {
            AssetType type = holding.getAsset() != null ? holding.getAsset().getType() : AssetType.CRYPTO;
            BigDecimal currentTotal = allocationByType.getOrDefault(type, BigDecimal.ZERO);
            allocationByType.put(type, currentTotal.add(holding.getTotalValue()));
        }

        if (totalBalance.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<AssetType, BigDecimal> entry : allocationByType.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalBalance, 2, RoundingMode.HALF_UP);
                entry.setValue(percentage);
            }
        }

        return PortfolioSummaryResponse.builder()
                .currency(targetCurrency)
                .totalBalance(totalBalance.setScale(2, RoundingMode.HALF_UP))
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .unrealizedProfitLoss(unrealizedPnL)
                .unrealizedProfitLossPercentage(unrealizedPnLPercentage)
                .realizedProfitLoss(realizedPnL.setScale(2, RoundingMode.HALF_UP))
                .allTimeProfitLoss(allTimePnL)
                .totalWalletsCount(userWallets.size())
                .totalAssetsCount((int) consolidatedHoldings.stream().filter(h -> h.getQuantity().compareTo(BigDecimal.ZERO) > 0).count())
                .allocationByType(allocationByType)
                .wallets(walletSummaries)
                .build();
    }

    /**
     * Consolida as posições de todas as carteiras em uma única lista de Holdings em USD.
     */
    public List<HoldingResponse> calculatePortfolioHoldings(String userId, Map<String, BigDecimal> currentPrices) throws Exception {
        return calculatePortfolioHoldings(userId, currentPrices, "USD");
    }

    /**
     * Consolida as posições de todas as carteiras em uma única lista de Holdings na moeda especificada.
     */
    public List<HoldingResponse> calculatePortfolioHoldings(String userId, Map<String, BigDecimal> currentPrices, String currency) throws Exception {
        String targetUserId = (userId != null && !userId.isBlank()) ? userId : securityCore.getCurrentUserId();
        if (!targetUserId.equals(securityCore.getCurrentUserId())) {
            throw new org.springframework.security.access.AccessDeniedException("Acesso negado: você só pode consultar os holdings do seu próprio portfólio");
        }

        String targetCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
        List<WalletDocument> allWallets = walletFireBaseAdapter.findAll();

        List<WalletDocument> userWallets = allWallets.stream()
                .filter(w -> isWalletOfUser(w, targetUserId))
                .collect(Collectors.toList());

        Set<String> walletIds = userWallets.stream().map(WalletDocument::getId).collect(Collectors.toSet());

        List<TransactionDocument> allTransactions = transactionFireBaseAdapter.findAll().stream()
                .filter(tx -> {
                    String wId = tx.getWalletId() != null ? tx.getWalletId() :
                            (tx.getWallet() != null ? tx.getWallet().getId() : null);
                    return wId != null && walletIds.contains(wId);
                })
                .collect(Collectors.toList());

        return computeHoldingsFromTransactions(allTransactions, currentPrices, targetCurrency);
    }

    // =========================================================================
    // MOTOR DE PROCESSAMENTO CRONOLÓGICO FINANCEIRO
    // =========================================================================

    private List<HoldingResponse> computeHoldingsFromTransactions(
            List<TransactionDocument> transactions,
            Map<String, BigDecimal> currentPricesOverride,
            String targetCurrency) throws Exception {

        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Cache de ativos para evitar N leituras remotas
        Map<String, AssetDocument> assetCache = new HashMap<>();
        List<AssetDocument> allAssets = assetFireBaseAdapter.findAll();
        for (AssetDocument a : allAssets) {
            assetCache.put(a.getId(), a);
        }

        // 2. Ordenar transações em ordem cronológica (datetime ASC)
        // Em caso de empate de timestamp, compras/entradas são processadas antes de vendas/saídas
        List<TransactionDocument> sortedTxs = transactions.stream()
                .sorted(Comparator
                        .comparing((TransactionDocument tx) -> tx.getDatetime() != null ? tx.getDatetime().toDate() : new Date(0))
                        .thenComparingInt(tx -> isCredit(tx.getType()) ? 0 : 1)
                        .thenComparing(tx -> tx.getId() != null ? tx.getId() : "")
                )
                .collect(Collectors.toList());

        // 3. Agrupar transações por ID do Ativo
        Map<String, List<TransactionDocument>> txsByAsset = new LinkedHashMap<>();
        for (TransactionDocument tx : sortedTxs) {
            String assetId = tx.getAssetId() != null ? tx.getAssetId() :
                    (tx.getAsset() != null ? tx.getAsset().getId() : null);

            if (assetId != null) {
                txsByAsset.computeIfAbsent(assetId, k -> new ArrayList<>()).add(tx);
            }
        }

        List<HoldingResponse> holdings = new ArrayList<>();
        BigDecimal grandTotalBalance = BigDecimal.ZERO;

        for (Map.Entry<String, List<TransactionDocument>> entry : txsByAsset.entrySet()) {
            String assetId = entry.getKey();
            List<TransactionDocument> assetTxs = entry.getValue();
            AssetDocument assetDoc = assetCache.get(assetId);

            AssetResponse assetResponse = assetDoc != null ? assetMapper.map(assetDoc) :
                    new AssetResponse(assetId, "UNKNOWN", "Ativo Desconhecido", AssetType.CRYPTO, null);

            BigDecimal currentQuantity = BigDecimal.ZERO;
            BigDecimal currentInvested = BigDecimal.ZERO;
            BigDecimal avgBuyPrice = BigDecimal.ZERO;
            BigDecimal realizedPnL = BigDecimal.ZERO;
            BigDecimal lastPrice = BigDecimal.ZERO;
            int txCount = 0;

            for (TransactionDocument tx : assetTxs) {
                TransactionType type = TransactionType.fromString(tx.getType());
                if (type == null) type = TransactionType.BUY;

                BigDecimal price = tx.getPrice() != null ? tx.getPrice() : BigDecimal.ZERO;
                BigDecimal qty = tx.getQuantity() != null ? tx.getQuantity() : BigDecimal.ZERO;
                BigDecimal fee = tx.getFee() != null ? tx.getFee() : BigDecimal.ZERO;
                BigDecimal amount = tx.getAmount() != null ? tx.getAmount() : price.multiply(qty);

                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    lastPrice = price;
                }
                txCount++;

                switch (type) {
                    case BUY:
                    case TRANSFER_IN:
                        BigDecimal costThisOp = amount.add(fee);
                        currentQuantity = currentQuantity.add(qty);
                        currentInvested = currentInvested.add(costThisOp);
                        if (currentQuantity.compareTo(BigDecimal.ZERO) > 0) {
                            avgBuyPrice = currentInvested.divide(currentQuantity, 8, RoundingMode.HALF_UP);
                        }
                        break;

                    case REWARD:
                    case STAKING:
                    case AIRDROP:
                    case DIVIDEND:
                        // Rendimento passivo: custo base zero, apenas taxa adicionada se houver
                        currentQuantity = currentQuantity.add(qty);
                        currentInvested = currentInvested.add(fee);
                        if (currentQuantity.compareTo(BigDecimal.ZERO) > 0) {
                            avgBuyPrice = currentInvested.divide(currentQuantity, 8, RoundingMode.HALF_UP);
                        }
                        break;

                    case SELL:
                        // Na venda:
                        // Preço médio NÃO se altera.
                        // Lucro realizado = Receita Líquida - Custo da parcela vendida
                        BigDecimal costOfSold = avgBuyPrice.multiply(qty);
                        BigDecimal netRevenue = amount.subtract(fee);
                        BigDecimal profitThisSell = netRevenue.subtract(costOfSold);
                        realizedPnL = realizedPnL.add(profitThisSell);

                        currentQuantity = currentQuantity.subtract(qty);
                        if (currentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                            currentQuantity = BigDecimal.ZERO;
                            currentInvested = BigDecimal.ZERO;
                            avgBuyPrice = BigDecimal.ZERO;
                        } else {
                            currentInvested = currentQuantity.multiply(avgBuyPrice);
                        }
                        break;

                    case TRANSFER_OUT:
                        currentQuantity = currentQuantity.subtract(qty);
                        if (currentQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                            currentQuantity = BigDecimal.ZERO;
                            currentInvested = BigDecimal.ZERO;
                            avgBuyPrice = BigDecimal.ZERO;
                        } else {
                            currentInvested = currentQuantity.multiply(avgBuyPrice);
                        }
                        break;

                    default:
                        break;
                }
            }

            // Determinar a cotação atual a ser considerada:
            BigDecimal currentPrice = null;
            BigDecimal change24h = BigDecimal.ZERO;
            if (currentPricesOverride != null && currentPricesOverride.containsKey(assetId)) {
                currentPrice = currentPricesOverride.get(assetId);
            } else if (currentPricesOverride != null && assetResponse != null && currentPricesOverride.containsKey(assetResponse.getSymbol())) {
                currentPrice = currentPricesOverride.get(assetResponse.getSymbol());
            } else {
                // Tenta buscar cotação de mercado em tempo real via Binance/Coinbase/Bybit/Brapi/Yahoo
                if (assetResponse != null && assetResponse.getSymbol() != null) {
                    try {
                        var marketPriceOpt = marketDataService.getMarketPrice(
                                assetResponse.getSymbol(),
                                assetResponse.getType(),
                                targetCurrency
                        );
                        if (marketPriceOpt.isPresent()) {
                            currentPrice = marketPriceOpt.get().getPrice();
                            change24h = marketPriceOpt.get().getChange24hPercent();
                        }
                    } catch (Exception e) {
                        log.debug("Cotação externa não encontrada para {}: {}", assetResponse.getSymbol(), e.getMessage());
                    }
                }

                if (currentPrice == null) {
                    currentPrice = lastPrice;
                }
            }

            BigDecimal totalValue = currentQuantity.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedPnL = totalValue.subtract(currentInvested).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedPnLPercentage = BigDecimal.ZERO;

            if (currentInvested.compareTo(BigDecimal.ZERO) > 0) {
                unrealizedPnLPercentage = unrealizedPnL.multiply(BigDecimal.valueOf(100))
                        .divide(currentInvested, 2, RoundingMode.HALF_UP);
            }

            HoldingResponse holding = HoldingResponse.builder()
                    .asset(assetResponse)
                    .quantity(currentQuantity.stripTrailingZeros())
                    .avgBuyPrice(avgBuyPrice.setScale(2, RoundingMode.HALF_UP))
                    .currentPrice(currentPrice.setScale(2, RoundingMode.HALF_UP))
                    .totalValue(totalValue)
                    .totalInvested(currentInvested.setScale(2, RoundingMode.HALF_UP))
                    .unrealizedProfitLoss(unrealizedPnL)
                    .unrealizedProfitLossPercentage(unrealizedPnLPercentage)
                    .realizedProfitLoss(realizedPnL.setScale(2, RoundingMode.HALF_UP))
                    .transactionsCount(txCount)
                    .change24hPercent(change24h)
                    .currency(targetCurrency)
                    .build();

            grandTotalBalance = grandTotalBalance.add(totalValue);
            holdings.add(holding);
        }

        // 4. Calcular alocação percentual de cada ativo sobre o total da carteira
        for (HoldingResponse h : holdings) {
            if (grandTotalBalance.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal allocation = h.getTotalValue().multiply(BigDecimal.valueOf(100))
                        .divide(grandTotalBalance, 2, RoundingMode.HALF_UP);
                h.setAllocationPercentage(allocation);
            } else {
                h.setAllocationPercentage(BigDecimal.ZERO);
            }
        }

        // Ordenar holdings pelo valor total de forma decrescente (maiores posições no topo)
        holdings.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));

        return holdings;
    }

    private boolean isTransactionOfWallet(TransactionDocument tx, String walletId) {
        if (tx.getWalletId() != null && tx.getWalletId().equals(walletId)) {
            return true;
        }
        return tx.getWallet() != null && walletId.equals(tx.getWallet().getId());
    }

    private boolean isWalletOfUser(WalletDocument wallet, String userId) {
        if (wallet.getUser() != null && userId.equals(wallet.getUser().getId())) {
            return true;
        }
        return false;
    }

    private boolean isCredit(String typeStr) {
        TransactionType type = TransactionType.fromString(typeStr);
        if (type == null) return true;
        switch (type) {
            case BUY:
            case TRANSFER_IN:
            case REWARD:
            case STAKING:
            case AIRDROP:
            case DIVIDEND:
                return true;
            case SELL:
            case TRANSFER_OUT:
            default:
                return false;
        }
    }
}
