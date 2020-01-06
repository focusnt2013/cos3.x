package com.efida.clc.crwal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;

public class CrwalPublisher {

	public static void main(String[] args) {
		JSONArray array = new JSONArray();
		int page = 10;
		for(int i = page; i <= 10; i++ ){
			try{
				parse(crwal(i), array);
			}
			catch(Exception e){
				e.printStackTrace();
				System.err.println("处理第"+i+"页出错");
				break;
			}
		}
		try {
			IOHelper.writeFile(new File("D:/focusnt/clc/trunk/DESIGN/采集数据/出版单位/"+page+".json"), 
				array.toString(4).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static void parse(Document doc, JSONArray array) throws Exception{
		if( doc == null ) return;
		Random random = new Random();
		Element table = doc.getElementsByTag("table").get(1);
		Elements trs = table.getElementsByTag("tr");
		for(int i = 1; i < trs.size(); i++ ){
			Element tr = trs.get(i);
			Elements tds = tr.getElementsByTag("td");
//			e.put("short_name", tds.get(0).text());
			Element a = tds.get(0).getElementsByTag("a").first();
//			e.put("gapp.gov.cn.url", a.attr("href"));
//			e.put("region", tds.get(1).text());
//			e.put("director", tds.get(2).text());
//			e.put("sponsor", tds.get(3).text());
//			e.put("category", tds.get(4).text());
			int j = a.attr("href").lastIndexOf("ID=");
			String id = a.attr("href").substring(j+3);
//			Thread.sleep(7000+(random.nextInt(10)*300));
			JSONObject e = getData(id);
			String name = e.getString("出版物单位名称");
			if( name.endsWith("出版社有限责任公司") ) {
				e.put("name", name.substring(0, name.length()-6));
			}
			else if( name.endsWith("出版社有限公司") ) {
				e.put("name", name.substring(0, name.length()-4));
			}
			else{
				e.put("name", name);//.substring(0, name.length()-6));
			}
			System.out.println(e.toString(4));
			array.put(e);
		}
	}
	
	public static JSONObject getData(String id) throws Exception{
		JSONObject data = new JSONObject();
		String referer = "http://www.gapp.gov.cn/zongshu/serviceSearchListbve.shtml";
		String url = "http://www.gapp.gov.cn/sitefiles/services/wcm/dynamic/output.aspx?publishmentSystemID=35&ID="+id;
		String pars = "pageNodeID=35"
				+ "&pageContentID=0"
				+ "&pageTemplateID=596"
				+ "&isPageRefresh=False"
				+ "&pageUrl=7e2NgF3U02dai0cwLqW7htXS0slash0u1npuTB5hF8OLkh5Q5borsJoRR0add0ANy44XM2BENKFiZMMPmJrq4tEH0slash0fYA78ww0equals00equals0"
				+ "&ajaxDivID=ajaxElement_1_384"
				+ "&templateContent=Gd8lAB8FKc24SsDPycoEr4FFZErm8oTkLNqsV5GnDXXi90add0Wy49JzUujAt8ODbWNs6rVl0yf9XJteWRpXJuPysr53FA2DviyRqjZvl20slash0FrkFto40gpdI4FgBu9hvYqsNPGKNKu1L0slash0m3nzicyNd1RDlE3Ql0add0LSApDK6WyChVmDtNpqNFP4q9NCJheMRehjCwYsEXni7ABBn0slash0QfPVUXFgG4NLI0slash0XzmWlnpOU0OjbsoVnqrJEMQZdotmuRQKRyNnw4fTlakQUEyzpZwex61TLSWcc7a8Vz761CiVnGPa1GcKMEFAiXlqnN0add0EpIVOf3ORQY1ZttSKOtB4iBVp5P09k2XZxlns0add0sPiuKugqTyyhVAVFQAxG30add0LSwf9ovilSeZ3yBcj3WdwrwqVkomnPVZPWQnaoVuTG9P2pozAMw0add0Dw5Eu0add0u8P0slash0MWQRa5Mux17gdfsQ0slash0skHRo1yFhs0add0OBvEidwRKfVs44L9yVPNUhLN9Q1SSMkiY6p7lGTurBb0add0FC87qyNlrBgodZmSPjQfOqIeFWhKj0slash0IbswNxoltvj2GEBrUbydXWiY7exuAk80MdrX7ej5enl0ydLcsuGbocW9ZEw6T2hBLPPHfrCYBL37iCS63yodQBJmL9OcPE4YOdWCR3CiU6oFyZ7uBKpmI2tBsgFt1FaopD8siWsjFfkEczTsdQbxmNh0slash0Dy48lVDNP0add0EG7YlRGPIUkfETHnLTKugAiWw3T0add0JqMsS0add0gpKWnw4eL2rOJoccWrNOJWmEmmhUkEAHv0slash0rI3WoD3Y9RzMU1h0M6GOdt48LbMoiHt2i6rNWOv0add0XWmWUE4Ew0AUy4jGnjK80slash03iIHdh89kM9qb0rWgqXg8Pdse0slash0c2Pjy30slash0yhKdje0slash065TbHDq6DAXfB2sU2LMwOQHYHgkBya0OxUCI1QlzDoc6VvQD4vJpB4vbDHw0SGeO6K8c8MgPEAgkQucggnMhW0add0Pzq0add0YKCL323Kqw5lRuUFT0slash08rzvcOQWWWBwPn3m8qNbRB3gNQZUIjfXVszFDFKNAL67XzfV1GhvQAtgKrRXE3QWOp9g7KID2MxPVr6Y9igDSU50slash0vVWw3XWvY7unaJbxRItv2OzRIw5CXPgduCC4zZKrVpCEOf3bu1SLQgWx3VrUZPO1zIfbVPJ0slash0j75RXrL6NlBLTfPtsYJpImsk7uA1npUsJx6NFcc5qfcfMGuKzYjZ0slash03gNB2WSk1kwOkYZM4GeciGSfwcT93Gwl4vM1l0slash0OH0add0bbgjuq40GIKoSNeXI1oRC0add0SPf4nSR6eHefZzUqHDUJYQzdbm0XOA9wJGgrW00slash0RRgRfQPFWMCCJqUD0bXde99o9mNSw7HsunHauPRjPWLE2lEQ62I0slash00l5uFmvgn5KM7RZNbrfhaAvChf7XKJ0add0vJHG0y2XhfO57iZ7G0slash0DCfj8rTHUIJZuhBxjlVl0w3kg7igk2d6ao0add0JmuyqO5ySHSt2AuggufrGwntwnwPwNDuq2lzGjQec47ZEp0add0xAXRUaE9rwRBqcrDORqCwAOU46XDmgm4S9lWBYRCgC7xBbP43etsldQOJjVmSaUFgy8P0add00slash0WvUztP0RaNL0TQmkjhmA49tXf724uu7tZ9898PQR4MnnjhhR3dGYiUM1K4uda2dGwiTo3hMToySNMsqJOz9rtLpyQ0add007l2uFYIEDjMqGMaw675rNsIWL7Ph9R0add08UL2Aik0slash0yPn4aFlEMXTWMQwdrlZNUfgnNeQIZRMzVIlhRSKBJGGDwBVi4vU6a9a4KrnXJuYbyGxF5oI51hV86wY0slash02w4Yqjz8m0add0z9woKKXS0add0G7sN0slash0z2uP0jWhTjOMHJ90add0qYxi8DhxIoiuLekVgyVcEnQz5zSzM85GUqzCq4zkt8OWelaodyF4AxBGF9HqxgvVWl7TQWgK0add0DF0slash0bdW1ftzJSWLpxkkntS4Qak0slash0NqvmFeMKVgQSybhndHyxFxXOqsEWn3iQ5WVgVuAnazyr6cErwXiOP6QIECret0fLaFZVrP77n0add0blo0add0e6fLQmjMzDN9a8yUaXrU00add0YmMebfJTSsOwgxgIDlMaFH6KdWnMtgKhlhc2VUNJ2M4E2EBXzDi0QBwvsIa08ApZYjfiZK1IQssL51naXY2K4IbdopZAYRNDQRASpLOEpk9XGy9x2vY7XmCcTasejoOM5vVDh40add0hSHPbuE77Yd26Uqt4jh0add0Fq3jTteMiFtsKCwhUNDnwFcJvMb6F9NFiF0slash020add00cBsfJKO1t9dJTat0add0pcDsAILvoPGFVEQ3W3TUov0Nc4onfK0slash03A67dECIv0add0QC2pt107MMyD96KR3LWNmFGLTExyZP9tmrqZ4IkUpBDxf6Uovlgw51hKkyaXFOwhkIMpFJw7ym2KYZ94SVNt7Duq9JD38i2cS9Mgp0slash0CPXe53tlY7uXR0EyRbm80add0YXyXLBvQsKZEz9S8ZHnM3qWn442dmuJqwdw3QoUZ2ycMo2XTaaNXj0uFT4GoLrz5SzpJqf0Agm1yDYveBRIFS5m8Jv4IF0add04G0add0RdKKW0ox8lqDzahgwE0add0IF0FXq70qsay1peeJTiZeyz3Sx2uDO0nEJOkxH6WZShnQCQXbGDbBULqGQZpF30slash0myH0add07x3CSVZ7UMawRiNpeZA2YdU7J70add0UEvZxzDWq30slash0K7OQEo1H59M0slash0Jh4bp0slash0DvS6WyVKa4yXEy5uSiSKMsHOQTqu0slash0G0add086U8YlXg6E8C6IhMsvv7b3WItBcRb0add0TZPadDDHmeUqzV3FQw0equals00equals0"
				+ "&timeStamp="+ System.currentTimeMillis();
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put("Referer", referer);
		requestParams.put("Host", "www.gapp.gov.cn");
		requestParams.put("X-Requested-With", "XMLHttpRequest");
		requestParams.put("Cookie", "Hm_lvt_f40b5cd7f29dab09f1284d654d4bb944=1544428068,1544428852; Hm_lpvt_f40b5cd7f29dab09f1284d654d4bb944=1544435446; tracker_cookie_35=True; tracker_cookie_datetime_35=2018-12-10%2015%3A53%3A4");
		requestParams.put("Content-Type", "application/x-www-form-urlencoded");
		Document doc = HttpUtils.post(url, requestParams, pars.getBytes());
		Element table = doc.getElementsByTag("table").first();
//			出版单位前缀 	978-7-900455
//			出版物单位名称 	湖南科学技术电子音像出版社有限公司
//			地区 	湖南
//			主管单位 	中南出版传媒集团股份有限公司
//			主办单位 	中南出版传媒集团股份有限公司
//			类型 	电子出版物出版单位
		Elements trs = table.getElementsByTag("tr");
		for(int i = 0; i < trs.size(); i++ ){
			Element tr = trs.get(i);
			Elements tds = tr.getElementsByTag("td");
			data.put(tds.get(0).text(), tds.get(1).text());
		}
		return data;
	}
	
	public static Document crwal(int page) throws Exception{
		String referer = "http://www.gapp.gov.cn/zongshu/serviceSearchListbve.shtml";
//			Document first = HttpUtils.crwal(referer, true, null);
//			System.out.println(first.html());
		String url = "http://www.gapp.gov.cn/sitefiles/services/wcm/dynamic/output.aspx?publishmentSystemID=35";
		String pars = "pageNodeID=35"
				+ "&pageNum="+page
				+ "&pageContentID=0"
				+ "&pageTemplateID=597"
				+ "&isPageRefresh=False"
				+ "&pageUrl=7e2NgF3U02dai0cwLqW7htXS0slash0u1npuTB5hF8OLkh5Q5borsJoRR0add0ADJIp5YH5GUs44hXiPN9AzI0equals0"
				+ "&ajaxDivID=ajaxElement_1_882"
				+ "&templateContent=OweE12syV30slash0rysOhbFiV08I4KWW9FtH6faqBIYLUkIEZkr6e9K8lp5RTgsyCVRXw70add0HvacLbKMZrCAeaB8bNLJJ0slash0GpZaxBv1BQ8PIy32GR9I0slash0czSFIJyxjPh4860add0maK2TvybWJq9PPWvgMgJDnnwx0add0pKeoJBqd5WkzvVh89anf1BfyBwYby4kuVb4fYL0add0eO1nBYZ4GqjhpKcBRICPpwvQEG140slash0AlS0BvG0add0xAbqN0add085Y0slash0LPphkjAQS4EJjO1t9wmXPt7l6d3rKD0add0yUFx9OGmHsCiygM1lTKVId4l0add0q0add0ZWzAyVu6wcQaAZ20add06UngZH2M7pocp8tIA060b3SLb7bIBT1PzBLH0Mjd0slash0td88DCEiZMwW0slash0e0BnYsuWjwzcUs6k5JOo9Ixwdj3VaJohps0add0CKQPZp4yR8PSRyBzWzDuccqDeLbg8rcD5KoGKOYlnMIRkJg3G864PP2dm0slash0NpsD5jIfmJd4i8jZ8lz0slash0OSoMsBbZ6McYgxJtMCBVDYvh6fjZcTtQg9cCXExk30add0skiANj0J0add0DcC4Qj3cxf4Yn54Cx8ARFMK80slash0cXhl9hYHcE7AtIY8qk50add0yG2PZfE45IYcsUkzYBrvqXaGzz0add0Aa1rwcUnf0r2krnyXR0uOOOjhM7gtr6BVNltlzO0slash0D0upuxzPN5tdyxQz9vkocso4YexnRB2rMgniGRJLUE7LtIZiTkD4PsQ2RVMa9pNAksfa7CmAFWhlppJFelYE35UK3JX1rDeoqv6wJ0slash0H32oh3pZUiR8KIHuYbOFdnTNRcC0yvVQrplrcPljIjA8y5TNNycoIn38DaHQnKqPl0vbUofkQAPsmRRZVSgVmSuUkFt0add0cttrhPGugrop9ODiuGBngvLYALW0d4qw922V0add0dHjbAbnkGiAckYnRlx97DFw7ZigM0ejMzWmmiHnf0slash0nfRFP3W0slash09qSEA1EA0add0hIH1Jyq1FcvM0Jz9ofncgVd3aTa1tHuB14uDxeLUKeJv0aa8Ji4PN9rU20add0GSgP5AukG3nASV0HLJi1ZggwdRh0add0AIER3wRV0add08q9etjTI0tq0add05TezukqxOsQNKNumQ5rIKBxF5cdLNqT2f3fUY2XEiiBruAmx0JqTeiBk4lPFw7pATJsba7j4sMkRCGWSTuUQyCmPldTCiVJzeAQjEskQlFnEEZ7S8gNoGRT0slash0uSWdOtKv183LAqzwdYbmRQ6bpVgpJZyCi0add0CSHC4MS3y1vjfdjrbovGfbqLHkBCihOM77ku0xhpGo0add0mIL9GKgkBmE26RpacqC0slash00dGhANhScOTfLPX3T5isaS2yfYemdfKwcyNDfWw10add0NAyWrbD7TfrKgnJhJWnrSUjewvXYidOxFr9hy5ZeLemEjJUj824mVwfntW0slash0qa8zJahWIcqcu8hfNz0C50xpKjvNW0pJEIRKMTVPhYwpZv9iv4rZiO2K2Xr14c4sN7gtlByZjMLh0GgQ6u0slash0aBLGQoKvqaTbkQTq2FbOVVMCpZJoDpdPnhwCYUKnIu8F0add0TjAuNorIryJQaMmI3WItvnY0aMH0slash0KZwivrMT4xmBIq9gnf3hF9GB0slash0dilbrWOF85mWnWGUYhjGkK0add05aSySrwDXdInUUg7FgRlz9Hoi9MLAJHuSdRdz9nws7oVu7x8ZR1bSRivwGzbYQIn9ksuf0add0vESEYSrmj5C2zfTzeDiK5geB030salEmS0yCg2vihEWp7PWGfD8DNeHoBVLZ70slash0OW0slash0R4N6fiZnUwDur8i3OuVBHAy8FKFQBsxIwlwxHZVOjTyxG8bcQUMWYAE7MNRNSYAQwJ6RubGH7nPjRP8KChHDXu9VeXgoel7EAp7aR7iRnIPMQXijBg5lFIZ4FWZE89zd7AmNI25pBqOGMUg1fWw0slash0t0slash02AAfL0add0x5WoAqVyUPe4QySohtHBeaGgF0arfGOaGuxEQgXO0add0k8NwGQeyMr5AJcAq2zcsM9P4kyzI7QmIRMMfeJnm8w90add0pZx1WnTesiDrC5YUcPJssJzIP40add0TpdjjAWR0add01Y9Mk8HpAjLkUH20add04uAS2UsA2APuXHj2iWGujtDqch0add01S1HvE7xiSQizWlAAsy0slash0rOQ0dcVR0DR0slash0sGYNJfc8jFJlO7cD4EuXG5T6E9Otg0add07SgPQJGuO7k0add0ZJ0add0r8798bT2FDdJGs7DhSPk0add0pDB7dShXOEAZ6zs16uLa7LoNqP7D4WaJMFEKk1ZZViyBLFkIdd3sVKFNKCvWG4yYRs4jC54JlFQIEmDkHkxGEGvAXWUtkE48YRVJVF6IBSrH8mmrzmisiEu9h1i8vMJMZWqG1ZbJ7dJ20add0Jy3joTNw9Pr7sDPUNKRp0slash0hRoi0slash0x6c1q7hAbrso8UrQa0ZiMySq4QQgPSERgVepCexn8k9RHWuRGpdaEG7oiAtozgmXc0slash0sNu1o0add04fXQPqJ0slash0ByRXHgHDGKr8bmv4jsjajBkFXIB0add05B0add0i54XBtZzl7yDh8mYw0eW9OZaiBtWJa0add0juYCIYIjGfl81OD4062P1C6Ccn0add0frl9GdiFh4ZoLg5ad04yJFV8Ao0slash0CVB7rUKnXZnqW5hHYul3lKEgvMhFv4590add0mX1AbuuCj4D9iprc0slash0NmcOSpyECzQrU6jNMx7jCbKkEkdlmPI5PF5len61pePr5WKjl1NlbGACU8GtWBxC9UW8CuEg0GCK54FLGo49hEQ0XpMfjKmJ4S8FZb0slash0p3sOq9rFowS1uZXA20PwtCw04e9o90slash0vJzDw0slash0JEWCkI39Q1aav41nxp0add0zLj8Ml4yZPirUhjX64w6N0add0g0CdzhLLbleeqR54g2Db0add00add0X5g8ISWlsigVJUdpe3x8hSmAW8EMrz1ajFRP0DxnQJtuxmylO0UOUxtOsjpCSgg5mUAvij7v9acbSrtB3zaSCvJuruzNMT8u6nv3tG8CyU1DmarIfaujthUGdQtlrZoG3xMEUaYU3hD0add0ECnOCZP9ESjy0srUltjSJ3XquGZ0add0QaNtjNlXTVe14pW0add0vtoRUj5mx80hUkqgt54lD4R3DJs0slash05gS353N6CpjQNC6ZSobYT4zD57MfNGJpmum8rLr5sMX0add00vDa45o8xlfZclnlLKv7ynSNCZrIPkcHC2zkgeTRgLeQp7yTnMyHOje13U4VSPG7CsiBExCdfLEorIJJ0slash0l4VH9cSDGyTvKvKNOkY3UwmlN4UMSmPoihtAhJnmUpLTsnlRHmcviLL7EzYoIIIPoq4hvtePm7ZU0rctA2g6Nn3PlK06HZ4e6sCPPtX1y2O0slash0w3igoemueMcoSqCibuK0add0ia1WXMsuZbaNWNlddEbgAM5TkzsOEDSDvRGut0add0AU9uWcmgv8TYI0slash0OlZgOdEh2CpcwMMnIXY6HVeUwQgn1gkrvwXZCTSQVfyt0HYhjZoiZAxDnWoiwXPy7nztZm0slash03jbFTpXtzfQltLY5Klsx9YXrkKKDWERHYPOHJKlKkeUt91LRNb5utCLC8mnOoh0slash09zbuOe2t19NrHfLuMeJqrErRI4TWvq0slash09H3f0slash0skD70add0k0add060add0L0slash0KjBiT7v7lQdOFS4pwIn5tVrAUpB0slash0sXmv9qPYXaH0yeYseWj7Vmm70add0Y445fj2EY40add0XyuaU9J0slash09Pn520vjepI0gYJrdFZU5BzQqVgk1vDXiv8tdJLBSJhYoYQX8V1mWrxNQo3c1OvRs30slash0XBJiF1X6ftV4AEVipOZ0add04CcGZomSOLufqSjMHBE75cfWqE2LpBgmTFxIkYwcPOd2Ha6uchmTU9t07jbpebNegR7H6knYnD7WEmQwZeXgiH1j72uWFXC5bWh8wtpLV1dspRCxYAlp0slash08KOziU9gBo0add0JpcZdNgkFLuoehUMvDpCr80add02jrPFaI4r0slash0SJ0leMQ7aUnSLOHECrxdrXZGs3q4fvdLTwZf2L8BTwBz1230add00slash0560slash00slash0Qk1xJ0add00add06CBy49RsdJQELQRSMxFL0add00qq0slash0UpRs2tJO26C76MQhDEv4PtYdJAh8Ch8qdStgQUm1vyqtK3nFHdNNJaZXkDCXqmAQd56LCSV9QDxH0slash06qTfr9KkC1wfIAbL76a5zXeeQ0slash03qYTJCxnJqHuS81jRV0slash0ds6vgsEJlqHAu2X0slash0CP0add0OkcDqV0h5VmycCioUHdiuxf3EHSTWKtaw8P6SZvlPyP4wYyph2lMy0FOoBxlm0gaZvKalWPuq1cBKTg8n029YssxODjG0slash0bU1oiTfpJUPO4JfUzxuaNPw3QPO0j2i0slash0JPlrPLe0OrQ4gd9xZox49mHm0slash0DlMowqbciSFXP1EBlKj9655NgvVQp75QvplccbDEvEHwJMrCp0RAZIJsJKTxBVy5xDdLUQeu9AESClAPDYv9XkIvKJ70add0KKiC29KAMs7e6bR0slash00ovjb0add0cGY0OoIvkVyyy4dFqRrtun0slash0ZpAFm37EoC56CL477UHjukVwTygquQmnqu6oBKykHXVNZUeG2w0add0010UJLCq1y2O0F4CRqKZI0DyC2Wn6OF0add0QA0slash0ivgZYManBRzRzfZKXV1iX0slash0Fuq30add0pKRUwbhkEKd7ifPoo0slash0ArJ0add0tJ4kycxIwP0slash0xCuSX0W2ZB0slash0OreyW0add0V9WLGRFDO3OOwVbm37Ca03DUpVskLtHL4jdUg8EeVkMfk0h0slash0Ld4muIeBWGwIHwOs0slash0aPl0add0IB4zqGtaC6MD1tBsMroz8z0slash0CU0add0O0add0l0emzCLKhjSphGq0slash0Bve0slash0vd2ZzSA0eMfDRz0wIQVi0slash08RflrAWZlzTFGknS85fOHvjjgvCx2zOxpE8b9gElXWVkalOF5CPKTxaunPKlX59qrrk4UbPGL0add0DGcbm0EytFMn0slash0HqY1IKh61NUiRtL0add0gUfG7NNAMnFLr0slash0r0add0Szz88fKpopHrwttmf8HG1M7x6uqXXJm7cro673sIIicBRnDMAbUlwVnbQnLnwEpinHLTf0add0gqFF66k7tmkoV6OvKD80OYcbtYjK0add0ESCkMlGItwrsQoMf1lVHKmFrXVpSY9LvpGLrzzhT30Mkhf4f5E1ywJLLwqDQUbzMNruNsglOrof3bxNSljGxm1zFwrT3WeJllY9449tK9N9cpXwdegFEvhZxjiPf6OQ0WMhj0slash0hrY9hWwEu9cFzbXJ8kxE4nN0UyOna2DpqtJRcSUg1LURWc22gtQUj07tSrfM80add07oinFB02uDRMNYsIZ8uHT9skfwB1MCIY0slash01iA00slash0zlDXrqcLwAjq9clS7QXThr0add0qhI3xMO0add0OKwVgZX8PxgyM99nraMl8WHN8GNoo1GWByE9cYW5a3R4Hy0slash0hEtaMk9CIKmvCFkym0nhJQCsm0slash0syUhoudPzXUnRk8aYvMFcpUN5IxEd0FgKCFYa1RN0add08iN5VNqsQvFSGrg6jH0ZgWP0slash06ZV420slash03OCqlPMycruSzcrF0Fgvhc85sJWo4Zd6eMSlIVwHAEXl5DLoV6lmH0add0i0slash0fpFBpbq5YBSNvX3hJnTgmnp5iDQoVkxjotrabrHVmRuZc1dZv7L5djflCoNGq1jUUgOuPehbiCf99qsyixh00equals0"
				+ "&timeStamp="+ System.currentTimeMillis();
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put("Referer", referer);
		requestParams.put("Host", "www.gapp.gov.cn");
		requestParams.put("X-Requested-With", "XMLHttpRequest");
		requestParams.put("Cookie", "Hm_lvt_f40b5cd7f29dab09f1284d654d4bb944=1544428068,1544428852; Hm_lpvt_f40b5cd7f29dab09f1284d654d4bb944=1544435446; tracker_cookie_35=True; tracker_cookie_datetime_35=2018-12-10%2015%3A53%3A4");
		requestParams.put("Content-Type", "application/x-www-form-urlencoded");
//			Host: www.gapp.gov.cn
//			User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0
//			Accept: */*
//			Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
//			Accept-Encoding: gzip, deflate
//			Referer: http://www.gapp.gov.cn/zongshu/serviceListcip.shtml?CIPNum=&ISBN=&Certificate=&PublishingUnit=%E9%87%8D%E5%BA%86%E5%87%BA%E7%89%88%E7%A4%BE&ValidateCode=FXC4
//			Content-Type: application/x-www-form-urlencoded
//			X-Requested-With: XMLHttpRequest
//			Content-Length: 8034
//			Connection: keep-alive
//			: 
//			Cache-Control: max-age=0
		
		return HttpUtils.post(url, requestParams, pars.getBytes());
	}
}
