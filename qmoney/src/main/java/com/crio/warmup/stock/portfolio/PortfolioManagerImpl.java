
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import com.crio.warmup.stock.PortfolioManagerApplication;
import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  private StockQuotesService stockQuotesService;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  @Deprecated
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotesService=stockQuotesService;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest


  //CHECKSTYLE:OFF
  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws StockQuoteServiceException {
        List<AnnualizedReturn> ar=new ArrayList<>();
        for(PortfolioTrade pt:portfolioTrades){
          String symbol=pt.getSymbol();
          LocalDate from=pt.getPurchaseDate();
          LocalDate to=endDate;
          List<Candle> cd = getStockQuote(symbol, from, to);
          Double buyPrice=cd.get(0).getOpen();
          Double sellPrice=cd.get(cd.size()-1).getClose(); 
          ar.add(calculateAnnualizedReturns(endDate, pt, buyPrice, sellPrice));
        }
        Collections.sort(ar,getComparator());
    return ar;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
        Double totalReturn=(sellPrice-buyPrice)/(buyPrice);
        LocalDate startDate=trade.getPurchaseDate();
        Double total_num_years=totalNumYears(startDate, endDate);
        Double annualized_returns=Math.pow(1.0+totalReturn, 1.0/total_num_years)-1.0;
      return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturn);
  }

  public static double totalNumYears(LocalDate start,LocalDate end){
    double days=ChronoUnit.DAYS.between(start,end);
    double total_num_years=days/365.0;
    return total_num_years;
  }


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  public static String getToken() {
    return "14e0e3fc7c4b9f6c7dd1bace39816b1b9d0ebb6c";
  }
  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws StockQuoteServiceException
  {
        // Candle[] candles = restTemplate.getForObject(buildUri(symbol,from,to), TiingoCandle[].class);
        // return Arrays.asList(candles);       
        List<Candle> candles;
        try {
          candles = stockQuotesService.getStockQuote(symbol, from, to);
        } catch (JsonProcessingException e) {
          // TODO Auto-generated catch block
          //e.printStackTrace();
          throw new StockQuoteServiceException(e.getMessage(),e);
        }
        return candles;
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?"
            + "startDate="+startDate+"&endDate="+endDate+"&token="+getToken();
            return uriTemplate;
  }


  // ¶TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.
  

}
