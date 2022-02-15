package nl.andrewl.coyotecredit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.andrewl.coyotecredit.dao.TradeableRepository;
import nl.andrewl.coyotecredit.model.Tradeable;
import nl.andrewl.coyotecredit.model.TradeableType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeableUpdateService {
	private final TradeableRepository tradeableRepository;

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	@Value("${coyote-credit.polygon.api-key}")
	private String polygonApiKey;
	private static final int POLYGON_API_TIMEOUT = 15;

	@PostConstruct
	public void startupUpdate() {
		updatePublicTradeables();
	}

	@Scheduled(cron = "@midnight")
	public void scheduledUpdate() {
		updatePublicTradeables();
	}

	public void updatePublicTradeables() {
		List<Tradeable> publicTradeables = tradeableRepository.findAllByExchangeNull();
		long delay = 5;
		for (var tradeable : publicTradeables) {
			// Special case of ignoring USD as the universal transfer currency.
			if (tradeable.getSymbol().equals("USD")) continue;
			executorService.schedule(() -> updateTradeable(tradeable), delay, TimeUnit.SECONDS);
			delay += POLYGON_API_TIMEOUT;
		}
	}

	private void updateTradeable(Tradeable tradeable) {
		BigDecimal updatedValue = null;
		if (tradeable.getType().equals(TradeableType.STOCK)) {
			updatedValue = fetchStockClosePrice(tradeable.getSymbol());
		} else if (tradeable.getType().equals(TradeableType.CRYPTO)) {
			updatedValue = fetchCryptoClosePrice(tradeable.getSymbol());
		} else if (tradeable.getType().equals(TradeableType.FIAT)) {
			updatedValue = fetchForexClosePrice(tradeable.getSymbol());
		}
		if (updatedValue != null) {
			log.info(
					"Updating market price for tradeable {} ({}, {}) from {} to {}.",
					tradeable.getId(),
					tradeable.getSymbol(),
					tradeable.getName(),
					tradeable.getMarketPriceUsd().toPlainString(),
					updatedValue.toPlainString()
			);
			tradeable.setMarketPriceUsd(updatedValue);
			tradeableRepository.save(tradeable);
		}
	}

	private BigDecimal fetchStockClosePrice(String symbol) {
		String url = String.format("https://api.polygon.io/v2/aggs/ticker/%s/prev?adjusted=true", symbol);
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.GET()
				.header("Authorization", "Bearer " + polygonApiKey)
				.build();
		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() == 200) {
				ObjectNode data = objectMapper.readValue(response.body(), ObjectNode.class);
				JsonNode resultsCount = data.get("resultsCount");
				if (resultsCount != null && resultsCount.isIntegralNumber() && resultsCount.asInt() > 0) {
					String closePriceText = data.withArray("results").get(0).get("c").asText();
					return new BigDecimal(closePriceText);
				} else {
					throw new IOException("No results were returned.");
				}
			} else {
				throw new IOException("Request returned a non-200 status: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BigDecimal fetchCryptoClosePrice(String symbol) {
		String date = LocalDate.now(ZoneOffset.UTC).minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
		String url = String.format("https://api.polygon.io/v1/open-close/crypto/%s/USD/%s?adjusted=true", symbol, date);
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.GET()
				.header("Authorization", "Bearer " + polygonApiKey)
				.build();
		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() == 200) {
				ObjectNode data = objectMapper.readValue(response.body(), ObjectNode.class);
				JsonNode close = data.get("close");
				if (close != null && close.isNumber()) {
					return new BigDecimal(close.asText());
				} else {
					throw new IOException("No data available.");
				}
			} else {
				throw new IOException("Request returned a non-200 status: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private BigDecimal fetchForexClosePrice(String symbol) {
		String url = String.format("https://api.polygon.io/v2/aggs/ticker/C:%sUSD/prev?adjusted=true", symbol);
		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
				.GET()
				.header("Authorization", "Bearer " + polygonApiKey)
				.build();
		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			if (response.statusCode() == 200) {
				ObjectNode data = objectMapper.readValue(response.body(), ObjectNode.class);
				JsonNode resultsCount = data.get("resultsCount");
				if (resultsCount != null && resultsCount.isIntegralNumber() && resultsCount.asInt() > 0) {
					String closePriceText = data.withArray("results").get(0).get("c").asText();
					return new BigDecimal(closePriceText);
				} else {
					throw new IOException("No results were returned.");
				}
			} else {
				throw new IOException("Request returned a non-200 status: " + response.statusCode());
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
