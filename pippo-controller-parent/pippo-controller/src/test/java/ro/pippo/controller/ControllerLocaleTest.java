package ro.pippo.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Locale;

import org.junit.ClassRule;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;

import ro.pippo.controller.extractor.Param;
import ro.pippo.core.util.StringUtils;
import ro.pippo.test.PippoRule;
import ro.pippo.test.PippoTest;

/**
 * Test if Locale in request context is used to perform a numeric conversions.
 */
public class ControllerLocaleTest extends PippoTest {

	private final InternalLocale PT_BR = new InternalLocale(new Locale("pt", "BR"));
	private final InternalLocale EN_US = new InternalLocale(new Locale("en", "US"));
	private final InternalLocale FR = new InternalLocale(Locale.FRENCH);

	private final String VALUE_1 = "2400.50";
	private final String VALUE_2 = "0.5";

	private final String EXPECTED = new BigDecimal(VALUE_1).add(new BigDecimal(VALUE_2)).toString();

	@ClassRule
	public static PippoRule pippoRule = new PippoRule(new PippoApplication());

	@Test
	public void testLocaleBigDecimalPtBR() {
		Response response = given(PT_BR).get("/sumBigDecimal");
		assertResponse(response);
	}

	@Test
	public void testLocaleBigDecimalEnUS() {
		Response response = given(EN_US).get("/sumBigDecimal");
		assertResponse(response);
	}

	@Test
	public void testLocaleBigDecimalFr() {
		Response response = given(FR).get("/sumBigDecimal");
		assertResponse(response);
	}

	@Test
	public void testLocaleDoublePtBR() {
		Response response = given(PT_BR).get("/sumDouble");
		assertResponse(response);
	}

	@Test
	public void testLocaleDoubleEnUS() {
		Response response = given(EN_US).get("/sumDouble");
		assertResponse(response);
	}

	@Test
	public void testLocaleDoubleFr() {
		Response response = given(FR).get("/sumDouble");
		assertResponse(response);
	}

	@Test
	public void testLocaleFloatPtBR() {
		Response response = given(PT_BR).get("/sumFloat");
		assertResponse(response);
	}

	@Test
	public void testLocaleFloatEnUS() {
		Response response = given(EN_US).get("/sumFloat");
		assertResponse(response);
	}

	@Test
	public void testLocaleFloatFr() {
		Response response = given(FR).get("/sumFloat");
		assertResponse(response);
	}

	/**
	 * Assert if response is ok (http status code = 200) and if expected value is
	 * correct.
	 *
	 * @param response
	 *            response
	 */
	private void assertResponse(Response response) {
		response.then().statusCode(200);
		assertEqualsNumericValue(EXPECTED, response.body().asString());
	}

	/**
	 * Two numbers as text that are equal in value but have a different scale (like
	 * 2.0 and 2.00) are considered equal by this method.
	 *
	 * @param expectedNumber
	 *            expected number as text
	 * @param actualNumber
	 *            actual number as text
	 */
	private void assertEqualsNumericValue(String expectedNumber, String actualNumber) {
		final BigDecimal expected = new BigDecimal(expectedNumber);
		final BigDecimal actual = new BigDecimal(actualNumber);
		if (expected.compareTo(actual) != 0) {
			final String message = StringUtils.format("Expected '{}', but was '{}'", expected.toString(),
					actual.toString());
			throw new AssertionError(message);
		}
	}

	/**
	 * Format text number {@code numberWithDotAsDecimalSeparator} to {@code locale}.
	 *
	 * @param locale
	 *            Locale
	 * @param numberWithDotAsDecimalSeparator
	 *            number as text with dot decimal separator
	 *
	 * @return number formatted according {@code locale}
	 */
	private String formatToLocale(Locale locale, String numberWithDotAsDecimalSeparator) {
		final BigDecimal number = new BigDecimal(numberWithDotAsDecimalSeparator);
		final DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(locale);
		formatter.setMinimumFractionDigits(number.scale());
		formatter.setMaximumFractionDigits(number.scale());
		return formatter.format(number);
	}

	@Path("/")
	public static class SumTwoNumbersController extends Controller {

		@GET("/sumBigDecimal")
		public void sumBigDecimal(@Param("value1") BigDecimal value1, @Param("value2") BigDecimal value2) {
			final BigDecimal result = value1.add(value2);
			log("[ControllerLocaleTest] Raw data from request: value1: {} value2: {}",
					getRequest().getParameter("value1"), getRequest().getParameter("value2"));
			log("[ControllerLocaleTest] Result: {}", result.toString());
			getResponse().send(result.toString());
		}

		@GET("/sumDouble")
		public void sum(@Param("value1") double value1, @Param("value2") double value2) {
			final double result = value1 + value2;
			log("[ControllerLocaleTest] Raw data from request: value1: {} value2: {}",
					getRequest().getParameter("value1"), getRequest().getParameter("value2"));
			log("[ControllerLocaleTest] Result: {}", result);
			getResponse().send(Double.toString(result));
		}

		@GET("/sumFloat")
		public void sum(@Param("value1") float value1, @Param("value2") float value2) {
			final float result = value1 + value2;
			log("[ControllerLocaleTest] Raw data from request: value1: {} value2: {}",
					getRequest().getParameter("value1"), getRequest().getParameter("value2"));
			log("[ControllerLocaleTest] Result: {}", result);
			getResponse().send(Float.toString(result));
		}

		private void log(String str, Object... args) {
			System.out.println(StringUtils.format(str, args));
		}

	}

	public static class PippoApplication extends ControllerApplication {

		@Override
		protected void onInit() {
			addControllers(SumTwoNumbersController.class);
		}

	}

	/**
	 * Start building the request part of the test. This request specify
	 * {@code Accept-Language} header and two fixed parameters.
	 *
	 * @param internalLocale
	 *            instance of {@link InternalLocale}
	 *
	 * @return a request specification
	 */
	private RequestSpecification given(InternalLocale internalLocale) {
		return RestAssured.given().header(internalLocale.acceptLanguage)
				.queryParam("value1", formatToLocale(internalLocale.locale, VALUE_1))
				.queryParam("value2", formatToLocale(internalLocale.locale, VALUE_2)).when();
	}

	/**
	 * Helper to store {@link Locale} and {@code Accept-Language} header.
	 */
	private class InternalLocale {
		private Locale locale;
		private Header acceptLanguage;

		InternalLocale(Locale locale) {
			this.locale = locale;
			this.acceptLanguage = new Header("Accept-Language", locale.toLanguageTag());
		}
	}

}
