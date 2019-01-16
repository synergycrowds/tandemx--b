package tandemx.rdm.main;

import tandemx.db.DBAMarketData;
import tandemx.db.DBAMarketDataHib;
import tandemx.db.util.Constants;
import tandemx.db.util.MapsCreator;
import tandemx.model.Exchange;
import tandemx.model.Symbol;
import tandemx.rdm.datasource.KaikoCredentials;
import tandemx.rdm.datasource.KaikoHelper;
import tandemx.rdm.obtain.MarketDataObtainer;
import tandemx.rdm.util.DataDifferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Engine {
    public static void main(String[] args) {
        if (args.length <= 0) {
            System.out.println("Tree ID required");
            return;
        }
        try {
            Integer treeId = Integer.parseInt(args[0]);
//            RDMTreeParams params = getParams(treeId);
            (new Engine()).run();
        } catch (NumberFormatException ex) {
            System.out.println("Tree ID must be an integer");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void run() {
        long millisecondsToWait = 60000;//params.getWaitBtwSessions();
        while (true) {
            System.out.println("Session started");
            runMarketDataObtainingSession(Constants.DB_NAME_BASE_MARKET_DATA_KAIKO);
            System.out.println("Session completed");
            try {
                Thread.sleep(millisecondsToWait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void runMarketDataObtainingSession(String dbName) {
        DBAMarketData dbaMarketData = null;
        try {
            dbaMarketData = new DBAMarketDataHib(dbName);
            KaikoHelper kaiko = new KaikoHelper(KaikoCredentials.API_KEY);
            MarketDataObtainer marketDataObtainer = new MarketDataObtainer(dbaMarketData, kaiko);
            marketDataObtainer.obtain();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbaMarketData != null) {
                dbaMarketData.close();
            }
        }
    }

    private void runGetAllExchanges() {
        KaikoHelper kaiko = new KaikoHelper(KaikoCredentials.API_KEY);
        DBAMarketData dbaMarketData = null;
        try {
            List<Exchange> exchanges = kaiko.getExchanges();
            dbaMarketData = new DBAMarketDataHib(Constants.DB_NAME_BASE_MARKET_DATA_KAIKO);
            dbaMarketData.insertExchanges(exchanges);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbaMarketData != null) {
                dbaMarketData.close();
            }
        }
    }

    private void runGetAllSymbols() {
        KaikoHelper kaiko = new KaikoHelper(KaikoCredentials.API_KEY);
        DBAMarketData dbaMarketData = null;
        try {
            dbaMarketData = new DBAMarketDataHib(Constants.DB_NAME_BASE_MARKET_DATA_KAIKO);
            Map<String, Integer> currencyTypes = MapsCreator.createCurrencyTypeNameToId(dbaMarketData.getCurrencyTypes());
            List<Symbol> symbols = kaiko.getAssets(currencyTypes);
            dbaMarketData.insertSymbols(symbols);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dbaMarketData != null) {
                dbaMarketData.close();
            }
        }
    }

}
