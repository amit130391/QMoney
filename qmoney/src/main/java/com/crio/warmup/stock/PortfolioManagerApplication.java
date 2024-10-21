
package com.crio.warmup.stock;

import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Task:
  // - Read the json file provided in the argument[0], The file is available in the classpath.
  // - Go through all of the trades in the given file,
  // - Prepare the list of all symbols a portfolio has.
  // - if "trades.json" has trades like
  // [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  // Then you should return ["MSFT", "AAPL", "GOOGL"]
  // Hints:
  // 1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  // Check if they are of any help to you.
  // 2. Return the list of all symbols in the same order as provided in json.

  // Note:
  // 1. There can be few unused imports, you will need to fix them to make the build pass.
  // 2. You can use "./gradlew build" to check if your code builds successfully.

  private static PortfolioTrade[] readValue;


  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    String File = args[0];
    String contents = readFileAsString(File);
    ObjectMapper objectMapper = getObjectMapper();
    readValue = objectMapper.readValue(contents, PortfolioTrade[].class);
    // return Stream.of(readValue).map(PortfolioTrade::getSymbol).collect(Collectors.toList());
    List<String> al = new ArrayList<>();
    for (PortfolioTrade pt : readValue) {
      al.add(pt.getSymbol());
    }
    // System.out.println(al);
    return al;
  }


  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.


  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI())
        .toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  private static String readFileAsString(String filename)
      throws UnsupportedEncodingException, IOException, URISyntaxException {
    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()), "UTF-8");
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  // Follow the instructions provided in the task documentation and fill up the correct values for
  // the variables provided. First value is provided for your reference.
  // A. Put a breakpoint on the first line inside mainReadFile() which says
  // return Collections.emptyList();
  // B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  // following the instructions to run the test.
  // Once you are able to run the test, perform following tasks and record the output as a
  // String in the function below.
  // Use this link to see how to evaluate expressions -
  // https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  // 1. evaluate the value of "args[0]" and set the value
  // to the variable named valueOfArgument0 (This is implemented for your reference.)
  // 2. In the same window, evaluate the value of expression below and set it
  // to resultOfResolveFilePathArgs0
  // expression ==> resolveFileFromResources(args[0])
  // 3. In the same window, evaluate the value of expression below and set it
  // to toStringOfObjectMapper.
  // You might see some garbage numbers in the output. Dont worry, its expected.
  // expression ==> getObjectMapper().toString()
  // 4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  // second place from top to variable functionNameFromTestFileInStackTrace
  // 5. In the same window, you will see the line number of the function in the stack trace window.
  // assign the same to lineNumberFromTestFileInStackTrace
  // Once you are done with above, just run the corresponding test and
  // make sure its working as expected. use below command to do the same.
  // ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 =
        "/home/crio-user/workspace/amit130391-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
    String functionNameFromTestFileInStackTrace = "mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";


    return Arrays.asList(
        new String[] {valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
            functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace});
  }

  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  // and deserialize the results in List<Candle>

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    String file = args[0];
    List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
    List<String> symbols = new ArrayList<>();
    for (PortfolioTrade pt : portfolioTrades) {
      symbols.add(pt.getSymbol());
    }
  //  RestTemplate restTemplate = new RestTemplate();
    String token=getToken();
    LocalDate endDate=LocalDate.parse(args[1]);
    List<List<Candle>> candles=new ArrayList<>();
    for(PortfolioTrade pt:portfolioTrades){
   //   Candle[] obj=restTemplate.getForObject(prepareUrl(pt, endDate, token), TiingoCandle[].class);
   //   candles.add(Arrays.asList(obj));
        candles.add(fetchCandles(pt, endDate, token));
    }
    List<TotalReturnsDto> trd=new ArrayList<>();
    int i=0;
    for(List<Candle> cd:candles){
      Candle c1=cd.get(cd.size()-1);
      trd.add(new TotalReturnsDto(symbols.get(i++), c1.getClose()));
    }
    Collections.sort(trd,(a,b)->Double.compare(a.getClosingPrice(), b.getClosingPrice()));
    List<String> sortedSymbols = trd.stream().map(x->x.getSymbol()).collect(Collectors.toList());
    return sortedSymbols;
  }

  public static String getToken() {
    return "14e0e3fc7c4b9f6c7dd1bace39816b1b9d0ebb6c";
  }

  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename)
      throws IOException, URISyntaxException {
        ObjectMapper oMapper=getObjectMapper();
        File file = resolveFileFromResources(filename);
        PortfolioTrade[] pt = oMapper.readValue(file, PortfolioTrade[].class);
        return Arrays.asList(pt);
  }


  // TODO:
  // Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     String symbol=trade.getSymbol();
     LocalDate startDate=trade.getPurchaseDate();
     String url="https://api.tiingo.com/tiingo/daily/"+symbol+"/prices?startDate="+startDate+"&endDate="+endDate+"&token="+token;
     return url;
  }


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.
  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  public static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    double openingPriceOnStartDate=candles.get(0).getOpen();
     return openingPriceOnStartDate;
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    double closingPriceOnEndDate=candles.get(candles.size()-1).getClose(); 
    return closingPriceOnEndDate;
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate restTemplate=new RestTemplate();
    Candle[] candles = restTemplate.getForObject(prepareUrl(trade, endDate, token), TiingoCandle[].class);
     return Arrays.asList(candles);
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        String file=args[0];
        List<PortfolioTrade> portfolioTrades = readTradesFromJson(file);
        LocalDate endDate=LocalDate.parse(args[1]);
        List<AnnualizedReturn> ar=new ArrayList<>();
        for(PortfolioTrade pt:portfolioTrades){
          List<Candle> cd=fetchCandles(pt, endDate, getToken());
          Double buyPrice=getOpeningPriceOnStartDate(cd);
          Double sellPrice=getClosingPriceOnEndDate(cd);
          ar.add(calculateAnnualizedReturns(endDate, pt, buyPrice, sellPrice));
        }
        Collections.sort(ar,(a,b)->Double.compare(b.getAnnualizedReturn(), a.getAnnualizedReturn()));

     return ar;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

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


  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       List<PortfolioTrade> portfolioTrades=readTradesFromJson(file);
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioManager portfolioManager=PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
      return portfolioManager.calculateAnnualizedReturn(portfolioTrades, endDate);
  }

  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    printJsonObject(mainReadFile(args));

    printJsonObject(mainReadQuotes(args));

    printJsonObject(mainCalculateSingleReturn(args));

    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

