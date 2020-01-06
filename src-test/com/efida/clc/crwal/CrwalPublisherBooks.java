package com.efida.clc.crwal;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.focus.util.HttpUtils;
import com.focus.util.IOHelper;
import com.focus.util.Tools;

public class CrwalPublisherBooks {

	public static void main(String[] args) {
		JSONArray publishersNotData = new JSONArray();
		int i = 0, j = 0;
		File publishersFile = new File(args.length>0?args[0]:"D:/focusnt/clc/trunk/DESIGN/采集数据/国家新闻出版广电总局图书数据/出版单位/重庆出版社.json");
		File outDir = new File((args.length>1?args[1]:"D:/focusnt/clc/trunk/DESIGN/采集数据/国家新闻出版广电总局图书数据"));
		int beginIndex = args.length>2?Integer.parseInt(args[2]):0;
		int scanSize = args.length>3?Integer.parseInt(args[3]):1000;
		int endIndex = beginIndex + scanSize;
		try {
			if( !outDir.exists() ){
				outDir.mkdirs();
			}
			File publishersNotDataFile = new File(outDir, "没有查到数据的出版机构.json");
			if( publishersNotDataFile.exists() ){
				publishersNotData = new JSONArray(new String(IOHelper.readAsByteArray(publishersNotDataFile), "UTF-8"));
			}
			File handleCursorFile = new File(outDir, "cursor_CrwalPublisherBooks.json");
			JSONObject cursor = new JSONObject();
			JSONObject publisher = null;
			if( handleCursorFile.exists() ){
				cursor = new JSONObject(new String(IOHelper.readAsByteArray(handleCursorFile), "UTF-8"));
			}
			else{
				cursor.put("i", beginIndex);
				cursor.put("j", 1);
			}
			i = cursor.getInt("i");j = cursor.getInt("j");
			cursor.put("pageSize", j);
			System.out.println("cursor("+handleCursorFile.getAbsolutePath()+"): "+cursor.toString(4));
			JSONArray publishers = new JSONArray(new String(IOHelper.readAsByteArray(publishersFile), "UTF-8"));
			System.out.println("Begin to crwal: "+publishers.length()+" from item["+i+"], "+j+"th page.");
			for(; i < publishers.length() && i< endIndex; i++ ){
				cursor.put("i", i);
				publisher = publishers.getJSONObject(i);
				String publisherName = publisher.getString("name");
				cursor.put("crwaling", publisherName);
				File publisherDir = new File(outDir, publisherName);
				if( !publisherDir.exists() ){
					outDir.mkdirs();
				}
				System.out.println("## "+i+" : "+publisherName+", "+j+"/"+cursor.getInt("pageSize")+" pages begin.");
				JSONArray out = new JSONArray();
				File outFile = new File(publisherDir, Tools.getFormatTime("yyyyMMddHHmmss")+".json");
				try{
//					publisherName = URLEncoder.encode(publisherName, "UTF-8");
					for(; j <= cursor.getInt("pageSize"); j++) {
						parse(crwal(j, null, publisherName), out, cursor);
						cursor.put("j", j+1);
						IOHelper.writeFile(handleCursorFile, cursor.toString(4).getBytes("UTF-8"));
						if( out.length() > 0 ){
							//写数据到文件
							IOHelper.writeFile(outFile, out.toString(4).getBytes("UTF-8"));
						}
					}
					JSONArray crwaled = null;
					if( cursor.has("crwaled") ){
						crwaled = cursor.getJSONArray("crwaled");
					}
					else{
						crwaled = new JSONArray();
						cursor.put("crwaled", crwaled);				
					}
					crwaled.put(publisherName);
					cursor.put("i", i+1);
					IOHelper.writeFile(handleCursorFile, cursor.toString(4).getBytes("UTF-8"));
					if( out.length() == 0 ){
						publishersNotData.put(publisher);
					}
					j = 1;
					cursor.put("j", j);
				}
				catch(Exception e1){
					e1.printStackTrace();
					break;
				}
				finally{
					//写数据到文件
					if( out.length() > 0 ){
						//写数据到文件
						IOHelper.writeFile(outFile, out.toString(4).getBytes("UTF-8"));
					}
					IOHelper.writeFile(publishersNotDataFile, publishersNotData.toString(4).getBytes("UTF-8"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 解析数据
	 * @param doc
	 * @param out
	 * @param cursor
	 * @return
	 * @throws Exception
	 */
	public static int parse(Document doc, JSONArray out, JSONObject cursor) throws Exception{
		if( doc == null ) return -1;
		Random random = new Random();
		Elements tables = doc.getElementsByTag("table");
		if(tables.size()<4){
			System.out.println("!!!Not found data.");
			cursor.put("pageSize", 1);
			return 0;
		}
		Element table = tables.get(2);
		Elements trs = table.getElementsByTag("tr");
		int size = 0;
		for(int i = 1; i < trs.size(); i++, size++ ){
			Element tr = trs.get(i);
			Elements tds = tr.getElementsByTag("td");
			Element a = tds.get(0).getElementsByTag("a").first();
			int j = a.attr("href").lastIndexOf("ID=");
			String id = a.attr("href").substring(j+3);
//			e.put("region", );
//			Thread.sleep(7000+(random.nextInt(10)*300));
			JSONObject e = getData(id);
			e.put("referer", "http://www.gapp.gov.cn"+a.attr("href"));
			out.put(e);
			System.out.println(e.toString(4));
		}
		table = tables.get(3);
		trs = table.getElementsByTag("tr");
		Element tr = trs.get(0);
		Elements tds = tr.getElementsByTag("td");
		String pages = tds.get(1).text();
		System.out.println("------"+pages);
		int i = pages.lastIndexOf("/");
		pages = pages.substring(i+1, pages.length()-1);
		cursor.put("pageSize", Integer.parseInt(pages));
		return size;
	}
	
	public static Document crwal(int page, String certificate, String publishingUnit) throws Exception {
		certificate = certificate!=null?URLEncoder.encode(certificate, "UTF-8"):"";
		publishingUnit = publishingUnit!=null?URLEncoder.encode(publishingUnit, "UTF-8"):"";
		String referer = "http://www.gapp.gov.cn/zongshu/serviceListcip.shtml?CIPNum=&ISBN=&Certificate="+
			certificate+"&PublishingUnit="+publishingUnit+"&ValidateCode=FXC4";
		System.out.println(referer);
		String url = "http://www.gapp.gov.cn/sitefiles/services/wcm/dynamic/output.aspx?publishmentSystemID=35";
		url += "&CIPNum=";
		url += "&Certificate="+certificate;
		url += "&ISBN=";
		url += "&PublishingUnit="+publishingUnit;
		url += "&ValidateCode=FXC4";
		String pars = "pageNodeID=35"
				+ "&pageNum="+page
				+ "&pageContentID=0"
				+ "&pageTemplateID=736"
				+ "&isPageRefresh=False"
				+ "&pageUrl=7e2NgF3U02dai0cwLqW7htXS0slash0u1npuTB5hF8OLkh5Q4nj0add0Au2USWbXsypRZC9EKJWkPS8qsz0slash08Q0equals0"
				+ "&ajaxDivID=ajaxElement_1_800"
				+ "&templateContent=7hd9T7y3BhlJWgbrzNvdq90slash0Tf2GW4jO7de4ktcN9YE1nr0slash0eOPk3rc0add0L1YO0slash00add02yM0slash00slash0Mr7C93wqA9726IEuYCHaWSP7r6gqsrVFsKv7cyI6XwhR6hV0t6MY7N0aGIlo8R2MTcNtwjc60wAntVaRAthxmxhe0add07Hd0ElgXg6ye0add0AEfMizzII3gm2QiM8y61jO6NWOPdCAbqFBLwngpyaGQ9Db8lhWBbbH4j73ykT3PRYEmt3xDsKh6GbrhfB6FoGhIiGPwPHXxrcBJ2oBvw0BSIXxsytWznLJ0slash0f6QN0add0q41RRBnyJfGI5HCuuIK5VR9YsMXd9mGLM6KUWrKV0flRCe5g2HGWPKiw50slash0MF5NG9N56PWT1nL7PtsUcRKi2RgW1EwOd5GU4cm00rRMcz6BRnWj9fUn5OAHgykJn80add0M8BtIoHUYXmVDJ7qjOsG7Z90add0CqEaa5Eozo3m0CnRDThJLGoAr0add0tROQ5IAeNd6FPr0cSpsb0slash0M3mumemm0slash0VscJTX0Wy1ngvBkVWNg1gyV9HPUvqKTRtVJv3aFXlwJgiHJkDNXyPizNfDQ0add0TG7dGyrEtDPvhCKc0slash0Mh2vt0slash0hGeVhW9EqtMGJv1nmCBYgeg1qUQ74qzDR5djg76T1av9ejYPqE9JDVb40t75EKJQrb5m3JnQqYbS4reP4FCB018d9Y4YWw0add0vNcRttFBVm0add0JZFkcYXzW9amzhNnyUyjgEyai0slash0NCxNO6moeZ3T4kgiJQR3EJkS3fDxjE0BmmQ7fA32nk8rUQOMiM7aFaspQ5x9Set7QHGiLU7DBFZteVkRVxehP7gyqpp9YdhxJYxgDBCDGjKtShIT1LSwnGB10add0GUgl0NrzOCwgiBxGsL3euAH7StYNu0BaImZShItG0add0yqGu4QncJ5Tw9vt24S01geQ2Vm8LpJkvuiwYj00V5dPybBvczUPRDwXB4D0add0SeP4VNlMRjB1Vc5160slash0v94KGI4LQA5hZhfzcxrtG0slash0OLE8eRXUueljYZKXpc4LrRBjJUyXLOiktg8dkVYJghwdIafCxCdQDyyckzK4RhnxVh7MMe5MtXwPUm4Wj6Jq7Dr2nrZOJZRzFQbBCFiRkCwfSt6AwwIZpx7shUXalpuWYBFrNJqepAt0add0J6IsQcde0add0zhjheOH8a0r4Wu0VZR20Fxg7M32wAhUIRlCSoJo0Z3awy1ORbsH0add0lgghO7L86JRJBuhPMqVYIjamMQMC8a581cLmSwuzI5ush2X0slash0ItXZxwG0add0xF5v5BOmQIjlepZ01zxm6HwbdKWykbZqXRCR0slash0vqZfTAEEviFchxuk3k1R5hdmbljdyOH4atI0slash0L3BTfw8lqpNU0rFOE0add0qzXeBEl0add0PmnGd0slash0BPFs2pmCIwFHN0slash0ZF3Feq5i3jvALnpzNH1Vya27eXf6bPS990add0KS99bBt12jN7Ia0slash0yZ5xeFY8LXN4KC0lj5LxOp0OOXfGpWeiwCJAcs2EwHJHr5eiHiPw0M5rldH5o88pMGMt5S82E2kxBcyTsp8AjbrUgxcWQD7GQy7Wy5icxNcSMMUQ7U3WPxoHL6vzcKvdyqRgcxM0HGs8IlHD0add08MlUhnOkSMyBUCRxy79KQ41sR0add0U0add0FWcB6Zcb8hgFrzSSsP7e0slash09JDoF0add0DjpdhPEIFDxH97p3eo0add0Bo6dwcDXHyYCa82UOpmY0slash0F1aLnFKDAtPZU7QWjYvqCmNHTKPukEuG0slash09CWyO0slash0JsiL7XXJU4TF9BfBLLiv3grGoXn117SHN50add0HT6rUPnh0add0gGoNe55M5qRMusxRaeU0TbNCvzpwJqZvtLStj0add0maWlxZOQ7yjnHO0add0thJ1mASGxQZNdo1OB0add0iWkcplZq52wPEFT17TYLJCoMfiPEFh4lJSTA0880add0eO099l9wH558NixqE20ydfEHH0add0MJqPZ6LqdkU3fMopk0slash0lf3j0Ox3q7HQFdFCQO8b1aFowxaS9C6B4BhkPlBBSLSctKKVydVFajqw8gKTRrRbNkvOjpRNuo15lVfZ0Xo8fombAWdQrRVWRZou7oGr20slash0i8KsV8YAtR68LExjvw1y2lHJ00OX0slash0Nb0staD243DVbEF0add0QRou66MExf1l0add0A1yD6yArFzObB0oj8u3X4jX8I0add0Kouqex0svmNrYiqED2EK5lVhaAKcjMDCdkppO6uqeuFNJ4YAI9KM3HfD0add0xxXMSR1ub0add0qehygSdcgbg8bX01KIkXoeknORvi2v0Zr3SzY0slash0s4xoJpwW7L80pkjwtHQwF6UsFHMVKa51x2SUZrlp0add00add0EzqJhk40slash0KRJ00add0mjcupG0slash0g2jFrXeILVP9jpQ0slash0FemJFPxNk5nNzFMrsskViDyE0slash0v0slash0wjzREIgxu4SN6BdQCe0add0RK3sV3n51gaZAWbGg4xlmYa0slash0Ldu32ns0slash0t7ql3jc0add0D0ac4CGP7jnOQ7u3RpgdMtM3r1QP0add0I9xHk1mQdOySn6DoiHRs0slash05ks7XFB1tSU28psWg58bgZrRnqmO2U0add0CQEboNhQT7DdB3kBToUQUBF6NoYzaBbp6yrxFv2fujWrPp0add0FP73OdDC8EaJ3vcDo6rU8flwWrTtnIMk9dw3x1pSitiagEOwEGFLlESvV6kGBQNGpjQ4AdUckBfJ49YgJhiuTZ70slash0xKS40IxjRzrhYYQGaUkx9z7JOFZUK10slash0KUy864w2x1oGDGdi6s5V980DtSx0add0P52XCRuOesXnCTvDBVLLGqAyPOllto9XbD4l8kMAHfYlJbAWnFdAezhOthlsBGF96X0slash06bjEXicm5xMiKVSL0slash0zPgUXDJjUujv7reQf5oLlBwqf9Pg2cHXq3xh0add003U5GQKiSiIWkIV3fqj1Feo6ZjwNHJuUkx7DwbFMY8M0R0slash0aCGek8F0slash0iomv0add07vBz5UreYm0slash0B0wfmzPFlJsfeSig2f48k0add0qiOIKhPREMYlIQZmfbT8JWC0add0JLJS9nT8IAzBZUkWeIkpefcw8E8QpOniTKx0KLcJy9dioey4PHp2PFJH50add0eaIdruB2F4DDgnD0add0tmWGWTadPkXjji0slash0xLX3QbwlE5svodCRsYBxRZ0slash03e8msq8onfzudd0ZX5XihzZzzhyS0Ln4DH3Pa5El8iWHw9tBetKKuMxaPokY3HWarKJ3h01luyz2sHIJ4YxQDK1DMBJESsdXqVMEhyiKeK74a0CK3UO8W9G6Jl6AY4JMLNGZo09WgOvv9UaDI90add0H0add0pLxcIrsxyt27s0slash0JPYH6yDOI02Gd0lXmxJHX53q332vxef9efJjHeCpNd0slash0ct2iBsjwmVZ3P3WgWnFHOcTeIdv5RkjukvnXOEa252WUzHbq8KGiDcGSUSmXaelYAp1k6t6Q3sBVCsd0slash0TQ10slash0Qs9CXcyvoR0slash0o7Jv2uyOfSEtNwz0slash0S0jZTqXF5ChLu0add0vdesj3KKJIe4Z0add00slash0sD620slash0wgaSya5WYC76ayhoaCV5j8knzgLjRlOJ2VQFAktP55nIl8ne6Cm0slash0OrjdoF4vFLrHMkNA0add0HZhQAfuCpQjXOJ82PA25IwDI3nJaOC4c0slash0PBFmz37qlJeb2V88KHvzTyH4mZdmKH37Fj0slash0T9DdNnH6ShKeqVErpK1RXEZiMna6G19j0slash0y5KIUAcE92Bxpl7QKpMHmlfJtqRdAl0rXHLrgDWCQOXCr735IKBzFHl4BsEeO0eESjC7kY45AdKfKoYg0slash0VD4Yz2tZW0slash0qAv54GoHF8I0add0U7BSNOZ8dnlAL2yDsPQxFQ0A6uM0add0LjsmAuXCJnZsmwAxFpZG0slash0VK330slash0EdJzBM30slash0WaqbPyV8TnikC0add0uxOh0nSc0add0agACh4VDEZU1myLIZVdPtOyq6G0add0mu3WpinRIKx3oR34EQ47E15c69aOYEKIPm7eTnfVr7LC5oP3oH9fXyWTkH7cEV2Gk8RadwEpfxPJdhg0YnBRqdSdDSf8gtI73Yewf0N38zptH5zmuhxZ13pmzEFfb6c2kkM0add0LN7T6qfY0nm7ap4heFuMdCSb9Tqnhftk4CtjUpbF3kNUlRjc6VSVpkFFatDHG1mNd1E6hMJw80add0ciriGagsokGOTdn0add04IE744tqEKumlHglUPsJuoXKL1K6GCXa0add0BJPzIXKliiNdPEhxno7Mgvvu2YlTIxwSt6HnJWKGUWP3uSJ0add0DcgeQkc070add0x5c5c5W0slash00mFVuXNHEjHtxNuwvzgE7qYRs0lWz5bTQGFLXQc7wDzGvokrsdWaZgoEunjqQWkL6QDft8J0g0slash0dK95iWN90add00add0OYEAfnKj09Let1VVfxYH8RHeDDj2tiRBE6ipwcNMQoeABZAuJTS5o08T4AmvZWnANUWgQ9cauGdlvYXVBGjjza420H76BhwSqyf4xtG5IBKZAkN3FCwZptQJb6Ni0yb20slash0vLvtLDnv4MaYtyPySxNIzHSDXXrTcNzOjSiRUqdv175Ck0GRpr1biVEC0add0qk2fPfF0slash0WGL8aCIgBLO3MRscdB0slash0noF30slash04dqjSHHS19WiYLh28ZN0add0dqK4mzZWJlH1MvHmSVjWPgwnHB6DeKeqYvPNqPA0slash0VXuNP0slash0Qx2H0add08pO0add0Qft8qRRzoO7JSWo6yXM2zf6nveuuq5LQEpPp2lFKgRtgeG1CnuxvSTfYPJPuEqTn71kDPafOTzSwDJoabmxzmMzH8pMBXA6f15UEcPww1Kp5wWnRdXKlb3M8X0slash0As5bVmPbq140slash0sjxb6Ba8Hu0SGsQbJjk2lZHSIkGrq29ezDmlsK0IJmF0add0ETSDgzEvI6Rzz5cz4Zou5YcZ0add0ICNwijok4S9guZS47tBVk8Rtx6DXP5UVtYi30add0WegowSSA0slash0eNHlWJxqaXYRML0slash0EWeEy8IutIdpw1ZTGCpHqcmgN3ptUbNdlXLVEwXAVPVhUHWApL15lb3h9Mx80add00add0nZnYw5GacRKYTf6oahNtP8vVqeON0slash00slash0FrBypMD0slash02Mc0FpWlH9tTazQfxc6RxJq0slash03YaXNkKnkr8bjiQqVe8rz43pEwnjXUu1Yj9a8gIg3bOjdJ1ruMh8Q25UZazTaRKCx7tqJt3PbPp160zk9gEN4qDpFzH7V0add02apFXUMwaUSSJS0add08KOovHbVic7qNGt6bnD7oLBmXjKLNfe2fQVd1UoOu4zpGNdCS7Q5UbsgXo9t5CPOMb0add0ecEHg05wRTAcSUinBQwve9l6diicWIbYrUauLxLh0YBsb8JOg7glbj8Bihht31YQSmZrroJ3roCLJIO8nglBYIyF8t9sjp9U27p3GgQZTgB6Qi0slash0zdB27EMC4zID0slash0Y9DVJIJ0FEtGCB0qi89uf7T0zvBW3RStm0slash0qYHuV0slash0nBTvdf9uP7xDzdaU5qNedslqcMw2OLsbN6fRvA0slash0tAmxDUXSH9bb8Ix5zLAL4i3dOgLRCgTZQsRTQ43T40slash0kmgnYONaIlSsLPxSFT1beDedvCBVKQOn3u4cUNGwRwVUxA6k40slash03lEks5Q5suTOe9mJqcxbz90slash0vZjrVY9HcrOwM6N77tubA0slash064G6EuYry1eAy2Xpoy3OaQ9ZZCVTmaCLGHc3vmlKnaZG9gIcQAgyOrw6u1iOgV4jzrTa0pckmIJnLsSnmoIID0add09VdHunrYD0slash0fQ0add0mUn2CaT8GCMOKq8OcXD0add0a7p7LHISZMSrwXfb2DgS94HkaIiMHQ899TcYn3tMOF5P0a0slash0tZTHdNjQkdy0B70j1vUqFHfFPjih0w9ucmSf3n2wO5krK5zIYioqQPHd7PzvTglwLaeLnxFM0slash0OfhsBCfWk45YhhK7qzQK99Vn2JPZcbKHeKfuf6u0add0wWrtrrvCqbcXeB0slash0WahF8QE0add0ElAPJfymx5UpEc55RfBflNU3MLAzEGfvcfzZKLoS80wmGeX7ku5MUyyE0slash0MYIK2KhwpaA9vKPs9H13mYkKg4TJuez6F40slash0V0slash0pZYJJd1ziVu9aHbgMKzN2QX0slash030GWLPGzGAZ1lm0o1UZ0slash0ca6VX5oEXdra4kdnJZDlZlRE6Ca90add01SF50slash0ISikTJqkp85W20slash0d0slash04deQfMtH8O00LJa79JpD3ePHxmP7IULVne5yGWN86gE3V2wQMs8YR2NjyFJ0slash0o96U9XI5HpuHY48tUCP53EVx7U0tTrIIKXvKWz60slash0QoSdTaxhUlQY6JHuWcD6clxjdPh7SfXWc71i4i8UUWcBJZNPRnADNnVVE1t0YfU0fsBkAPcWZw3qKWAx7VbvIwMTU1wiIeZuZgtVy8yKDp8nWWObwwS0add0oplvFcZdi6fn3BSTFo01D7AxGdD2q5T0slash0ymxURAyBK7UcJv1bcpDVyfnWdmJzYttj2sRRjxBzbKFvmNDYVAelNRW6zTfwU0slash0x52bXh5F4s4LjhSceqbUaMBTMJTB9Wq7a4M1Sg1K0add04hZxIS4WdknH0slash0xe6XIRfv1o0add0JhxYjivtwypoW02tIyCpZxTWKD6aqET2jkG50slash0FnBZyokILG40slash0hX0slash0O0add0KLIZkRXKnQysOovwyLM1NEc0slash0oH1S3YH6evvWgyNz6ooy0slash08mZboGWAMolOUPjh5Jw40add0R0add0rTs6ZWzni3nxdvR5M05qHYVmqiWf6JJVM7gaLMCMAG3VjgycN3eNC7aR8lckukFbrNIFjR4gIShtWmaYP9PwboHqpXlZwVZYcsEKZkSginNNp8KtEB97Up5ybbZBkf2Ba0jmJFrsOBtcuUdzSZ8fsDiX5MhOpJJJ0P5oYRBcsJeso0add0tDL0add0cNRk80add0A4pwYDHjZhpTIxfnqK2fRyrYTt32j8zrntdl9ZxjRMUb0slash0aXibxnnnRHKvCU3bR0add01B4cgQDH6qtsMEOUIIQoIxjC4yf0O4thKHETKzf8V0slash0080KNrcU8c270slash0UXsVky7WOEHZnokfd7ZmpUi0add0CbkPjj4ephiHK0slash0AJ7C02q64bgMAa315W4C1eH0add0JkHTu9c4Evhrh7l67vHt4xUkWjEvGDt8fGlDbG0fJJZwrHljq1BY0equals0"
				+ "&timeStamp="+ System.currentTimeMillis();
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put("Referer", referer);
		requestParams.put("Host", "www.gapp.gov.cn");
		requestParams.put("X-Requested-With", "XMLHttpRequest");
		requestParams.put("Cookie", "Hm_lvt_f40b5cd7f29dab09f1284d654d4bb944=1544428068,1544428852; Hm_lpvt_f40b5cd7f29dab09f1284d654d4bb944=1544435446; tracker_cookie_35=True; tracker_cookie_datetime_35=2018-12-10%2015%3A53%3A4");
		requestParams.put("Content-Type", "application/x-www-form-urlencoded");
//		Host: www.gapp.gov.cn
//		User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0
//		Accept: */*
//		Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
//		Accept-Encoding: gzip, deflate
//		Referer: http://www.gapp.gov.cn/zongshu/serviceListcip.shtml?CIPNum=&ISBN=&Certificate=&PublishingUnit=%E9%87%8D%E5%BA%86%E5%87%BA%E7%89%88%E7%A4%BE&ValidateCode=FXC4
//		Content-Type: application/x-www-form-urlencoded
//		X-Requested-With: XMLHttpRequest
//		Content-Length: 8034
//		Connection: keep-alive
//		: 
//		Cache-Control: max-age=0
		
		return HttpUtils.post(url, requestParams, pars.getBytes());
	}

	public static JSONObject getData(String id) throws Exception{
		JSONObject data = new JSONObject();
		String referer = "http://www.gapp.gov.cn/zongshu/serviceSearchListbve.shtml";
		String url = "http://www.gapp.gov.cn/sitefiles/services/wcm/dynamic/output.aspx?publishmentSystemID=35&ID="+id;
		String pars = "pageNodeID=35"
				+ "&pageContentID=0"
				+ "&pageTemplateID=737"
				+ "&isPageRefresh=False"
				+ "&pageUrl=7e2NgF3U02dai0cwLqW7htXS0slash0u1npuTB5hF8OLkh5Q40slash0wk65W3pdcbG91dFMMQijxfHeu2jVp2k0equals0"
				+ "&ajaxDivID=ajaxElement_1_673"
				+ "&templateContent=7hd9T7y3BhkEZ8M511meuyPqwZsMwhncWFzSgqnk90muZkDLLHtvkbNM40JQTNDj5s54fgm4cVcu96VHiGZofrPGnbOTjQI5Iw1y5zqCGaiYt2T2M0qd9TuLeEkvG64ASPqJKnT1G0slash0RloFvZzWr5j3VZUgvi6L0slash0Qhy6WbzLHX5X0add0M3O86ZXLKTH7UUbLOJQl1uahf945StHVOEOUS8UpBij10slash0t0slash05sWT173vsVEHNZxtdS30MBiCR1m95kwtEKNqypfzTEMcThIw34ckJwTeH8lxbYPsR8lmtVlAB3Jj2nLXuyyHjhByHpbeZgBSNBcy5XeLJ21T7pZguVkpOkiMAc0r4IQYY7kk5dar3GpfIuPgYe4OA9Bv4KzrYn4iCck3jKJTbdV0IZ8RV2y0slash0JWy7jg9czTwPySBcHZ82dHMykPyv1QLpvODnTSMHa00C7nV8iJk6QE0slash0OcO2wydlLscsYXYTfFM6zImqND0add0sMye5kdDphiiFg2rda8eeJJuNZ0ynUFJIrvFbJlck6i9WC0slash0dJothxd5OEVjBnYCt4CN1C2b0L2ohvbns5C7Ts4bo0slash0AYo0rCDYdU9zK6XmJ0slash0C4waSodMafOiDzMib0add0fkMfTRUXakkp54qSXyk9Y3h9eTRhZWjaioCz9o1UQ2SPjZYCVKYTKmYWukldTiCzcJ1L1eMdmm0UbsnoeomriX4OtHJ8UcMpgmnd8JcBegsLTurJBJ8kXw8UzZrV7HxmCrB6zDmJfIqYRCTSiqf0slash0Iaqj5nbai1xpNnYTCfz59Ruqh0TdTZ1lmxS2xruzgHlCUn850slash0LmYQ12c24eWanAVUUJNboEP0add0yRc8qrpzMWkce0slash0S22V0add0VsepyZ1GM0slash07aAlTvJb3y0jNeKPQacCna0add0JPowx8aM0slash0c0FqmcYBDYGppm5JqGW0slash0h0AD7dl1BW0slash00WU1NOj0HhErH1Z1pvSaRNwcTwdHJw7ylBxsBpslUHWl7jt0add017u315GVb4CbeyoGNuMInLE4p3TvBK6hiJw22DBJurbWHLqAzg6UDbwgoLVu8WKo5vO0slash0xw60add0BtIsJRibvFaLKnBwQ1mGne84Hu9v4zMl3I5a05h98hez5fbHCkB0lSNg78JTrMVWbbWX6RCfY30slash0MLAjZAgdgEtUfNfq07Vb1bHiyzS0add0Slk3EU4CUW6pveFr1aXH8eereQVxPVNQMhGvYXyyPeCCTWq0slash0cSFqJU4h6a6cXoOEB5aRD12sZgoecgcYpaIbMFn4lRvUUiELpHFA0add0Pm5tbjcgDknpqsM7a9M1Oqvolm6vMYeT5OlIbASJI8u0add0j0add047ib70add0sJ1HHuGKQIfk4DJwxKFuVsgILefzqmIBGSjp0slash0qY1pL60mpFEhFbUcioDQGijnpEJhK50add0jrHZg38J7As84m5JmBNiwYEpmMvczTXn6gvrGvYl3Ay3Iviv0slash0C5Qw0slash0e96yTq0add0MWJf2WE6NHKCqMfyM050slash0TDj2PqkroVjGgX0add0o4Dnp0slash0SFBMZmiphf7P3fdp0add0CSR0kwi2rCuMZLzne4hC4TKyAC4iopvSFtdunxdlHgcsdMWqHSI9o6cC8OyMnFtl1seHi0add0k50add0pPhcRyaxt1HKisS6Bb0slash0l7LlU1mKYAJnze5GJPV2rnxXBvDBqjSNkxX0qZJZ5MYLzibYeg8BFozZItYohX0add01JvIvV7CZlzmzt3xdcftfeNfLTt0add0niYhhhxQS4crXuh0slash03MafW0add0KqArIvFsScieses0OYU6JTXFoZ1lzJiu0pB4d00dgsISFWiE8z0add0Dgtd2WcMJXt16woCLVin7H5ggN3GQXUgJtKtciIRpczRg7YlAxbhk9TLT0add0U1LC6zVg9t0slash0KueeuvMMTKuBQapRfDTwhEEEajW32hiVH80slash0Ofr0add0D5M8x4uO74NZgaOw13hJYapFHxPZNKzkjROtcpnquTqLsZoWPtF0add0VGKpSfiXnpT8fC84u4obirDK1ZTz7GuYompw3tIU0slash0z0add0KPqA2st8Lj2JQ8lprCGyGk0rWKbPlrGzUemS1jv6yK3BxWhtmeGYt4gY1mZvC3QNc2MxfjOIqGXIHS0slash0dYp8GIUaJ70slash0tlt0slash0XG7nmybUMzqz0DQbKpyrZPjGW0add0Vf0slash0Ze3ws0add0U2AEY7USItMRGFyTXz0slash00slash0Wrg4s05ac40slash04GB3xKsVhkvjnMK2u2XtmS0add0H0add00add0lACGp7FOxYh8oXrRDZPmFa0add08snFWb9pBMQ1hmnWiDuqx7btbfH30add0jZTm5zwreNdKY6ga5n6banGZ4ogLS55MZn6IpXjv50add0QpJMBKM0slash0r64BJj9jQXkaQnnOF8NA6eqEBikRaELAgr2GqcWbnb1aPSvWZ7Aup0slash0oVqKBRO0vvchTZmeTkbQc8JmoVz4ZQFo34mSNcZlAStzp0add0GrszqE4q2NfZxJtJu0add01s9ZZ0qA3IK9X4DSIcAo6ZFOahP1eiPbEReXEgh8LnqUwbHFZuFSKO0uIDQ4YL4Hz0slash03GQAcMNgtgJ12yPVXjHc0slash0BmlvNQx1s9qsE2mTRNx3P9MUJkOt0add0suwviLa65WABI0add00slash0NjdsE5FAQa0add0coZbIYWQ9kMRJDGR3wHEZxXKsdoRvKRr0add06nVdOlgvMpP4U7EQDmqOtl7gxj0ocPtblSuKLe0add064exWn0slash0LMud6QP36e0slash0Nu0slash0DUdymqUKbenensp5fnI68TP7U2FhTql2WTm32Wbguk9Rf99uxEkqFEIZ0add09HREHn40KZG0XIYa0eSg0slash0ATVtOstkz2w5Cvpmh0add0s4SWNXM7bPCh4GoTERGJQnYcFjF6u25TC4GONWMd3MzFJa24xAlKJHuLoj8p9qy4zlhkQ0add025THrBmT3V6qyC3Vqp6rS572mzMarI1fkdRX5LbotIHma6tEmtUQcUqT1VCvGnLkZ6Y81eXFbeIKF0fzr0add0Cbi60slash0F0add0a4I2PQLeBidDGMGiuDa61qcqd0slash0sgCqASTT0Gd2iyTgALx1Vg5d7ofJfsXGPE0add0dhXv9L2QSKt70slash0yqKe8AEUcjafFH0slash0HT696PdQhAxy0add040slash00slash0fheF1HoSikplt0add0yx6zPWT9UzcV7YvM6QrH0add0qtXQn8pOU0add0yVkE0slash0Xj7my0lPYu7Fb79m7cwgRL3FuEQpY0slash0j1bCpe5tDvY1Vp56SvyCiJEGyIPhUIAAEoZu9gBgTDKyHUlmRwE1T53a0slash0chypUewPOgG5c80lmpCIOa0kXmk5pZ0slash0pI0add00add0hPMAU6EmSWLWPhWWyG5L0slash00add0FsVj0add0bhcvnYG47jaA8BenyjDRreA12kZCqUWm0add0HvcqicGO9q13sYR8fE0add0GCSGr7OY3uoY7UKRFbcivQo5F46xIQl5htAmlJUOfEVW0add00add0ngL0slash0zynaND0slash0G7IMaLLovgOsRmW694L6tDlMfTgUDkxRnY3J1v7AjrGeJntSCKJqBMwiiH3s0slash0ikZNgMYuNMxnRvBl8hF8R0JOwIoDSDVvvQAHAMDlK0add0hzqOZU3XfVbC00HuOkEdsM00slash0YHI6aq7L7YPjaUkiP6zUph0HjD8RMixhNTx80lNEtUYDzGU8xIM3jN7ijX0add0yioej2omw6N2iCEQutcfEpnUOMjeU75hImzPl0WA0icRV9w7ei3ci6RXsV4i6ulBJvrpB2tex4FAJY7sKldTC7vupYUPH0slash0Qy0flA0add06v42MJDOF1tPo42z32ZUY7U8Xhs17p6Jxuzg0slash0pVOmGZh0add0Do60slash0jqPURfv2Qw0slash0sYOidb6WXDnRJ1jRJxTHybNF8HXKFrcRI9SUoom9R0slash0splyVTINaGZy0TuAGGYaaEIa7w3MCOMwzKtI3sh4HifIcedgSvNlbgtjaBfxHJ9QI8eXAqtTPOSr0qSZceHU3KAQicfUdCkioq0slash0KeEksm1IoCckExXYWk80add0613BjcmR3lh20xuVmsj0slash0DpuK0slash0AsJvzE2xiH8h8wIZoBjAZa8BznqLvUekKjxykNWVYRZzSrXnEXM7scs8CX6FSUK0slash069ScDiEwVnEs0AXvLfYM7fCV8Lkkpp6RhFm5QOwkB1HB54oxuDMcroaVirxbv2wT0b9RulfqI9SWqflR0pr0add0SStpjzc10slash0T7kSNNllt4bLvN38JWKkNBiv5Dc0add02QcXmPZwJywWvM9u7q1DQcTYA7NCnrIj7An0slash00add0EdmDJWFwj10add0bumA2Tt40slash07Xmm3aPAVSE0add0Y8F0slash0bW0slash0huZZre66pO88ckMRQJlFtQYxW9SqjVWS1FkBhLz0slash0lWUWfL0taJpncs6YT6rQHEHARt4Exl8F5totVAu0slash00slash07h04QCBIrhizwX8RSuWPt8UXUQh0add0MPy5koY4RYxOapxlVauPCBiIduiG9wzeeBbhgQ0xDJWMQt380add0aXpPsk27D3uGwBRsznx7OwqDmqJJHR1YE0add0k1RbmzRsnE0SNDGf0sOKYwL1Wm78PVdGg410QSCcZrsYtHuL560slash0jRmfQaDJgBnswM0add0HWveA0add08kWU67Vuw3WaIyWwYuuHOaW4Bt8pkI5gFm1dkzcb7jFdyIBw2CW2xYMkZ6pil0kb6uNniYkLfOOCb6cr5VaX2PU2sFHEZMnDDZveYOKwp5ZHqggj0add0b9cikYKF0dht7poXfqDF6LuHD28XoLlBx3NrlEQYqmlukmVYllunjxvH2bNlXHMbwiBJ1vaxxKk6f9TBWAGsfTVaiBq6lUC6S9ckoz8DjEBrnp6jiPwFJq8FX3FipsFeWs0add09Okzm4mgU0add0O0slash0Od2BGmdaiP8xllC9dA5F0MXv1ECnDoJWMi0slash0X7WwLoc0add0Tbq07gW9x6K2YBcW3ad0slash08PirG0V5LtgK0slash0dYIpQjNuNOAlZGcpPhq62Rvma73SFSTdnIxacIGdAtU5XavOmcCMGNE8j3Hmx8WJOSkFtytoat9pzX7BrtLPC7cT9ujOx4y0slash0WWOFLIl2XJBWPE28LmK0kCdGK0slash0a5lyacnW8w7EXZfehynY3fIwYk9wiJuO7n5rcNr7hHfYemu0Uz0slash0elJu4qvg8T5Xg0slash0qww9M0slash0PCHF8ZWjw9eDjGknIb6xguzjyu7EQW0slash0RQrwkZwhvijgOMExyD6TnfT5mcQJ3lqtwwirhjNd0add0HKLP5rXsIsaAvf3GKtrdFjNAyj3ejpkhhihqjePCFHRuNEDCBW5jdDMUzdF0uEmvTa60add0Nf83W0slash0WYEnuN4m1uxy5d9Fv85NGc0add0wrsuAnZ0add0ZC0slash0Zm4YtXaTpvzIf1T0add0b6szWPNfuTqLDo8wKCzvh4ObMGBJQtb75pTTMXoWl0add07OM3LU82IvniSEUSafQS55iEGofb9jp5psbLot9oV0yFqIa9jPqGtxLq2MnxGT1u0slash0ttIscKQRLOp57ykaewTBhndGO3v8K0add0YQQPcYSPR7El1SzYtY0add0x7fDgnukKpjcy8dyobV0X8QRxYZQ5WTM3JvYMzZlESbV5LFboQFA5J0uXMJmSNKiGmD8wDZr6ccjE60add0ah5c860add0dz0slash0C66zczz7GD0add0d3l8UXQKkrALISTW0ZV6mG7vdnSHju0slash06zVWKC5mXT7t0O8aTL4lxScjW5xrtq0slash0nkGGMKkoGwBkP93eWAHGdWJK6rywWg0add0gMjcIiPPRhEH1SLPNeI0GRxMkI0slash0crOuz0add0TYXeDpvqs99LX9Rfc6ps333l8dG7ZKoRcIU6nHO3ekSqx3Vc0add0BAy4UofOg0slash0COtOLCcjwg7kU7fME70add0fY4g2APjAVfYG8p2bkJbPv8NI4WeqcWJP3BI5FuWtgjUAppVt0slash0a3jTlb5DbXFRISmHGHRz3gPmxVLCzmUc4h3wdR0slash0n5hY1a5Xv1lenTnxx3uXVUsYmagsWvJYYk0KBXGNJ0slash0xHL0add0CgkCSt0AZdvU5xNvWjB6VJAHkyef0slash0NnMUYqueJBFDWy4txTb5Ik8cj0slash04MRHedmQsKI2eEscZiljrr9t00mgooGMDYlRZEQvVV3DyXT00add03Hz8D0add0LCX1Ju0bo0rZnahz0slash0h3o8lWWvhmuvbRcnaG0slash0xACXxqGig04QG5Gg0QorwuUHbhxteDK7KqgslTBrnGvDZufaEenHSGu2CEtSpUGvMTZpO4QbPvZ0add0C7Szoteifk85lv63ewNk0slash0QI2BicFJF2rB0add0nY0sFqH3bPetfapZw70slash0Rm0gFJcMp7yg3dPNemqk0IvdTvdAMlYVDPAIqKPeanxYcuH4PwSABS2EYdPpC0add0SpoMBnO7E0xge8je8U7vixs0add0Qz7xqvJx2ivkvvA2G10slash0XB9Eclj0add0nlTSf4N41NZdmH9zIuXy9j45v0slash0nqssf7MoX8McXqXVGLiiuna9lmU0add0qBMaHErv64cmgVJ7RXSYNE7cphE0add0ltu59oPuifZzni0slash0oC8Ny8Po42uevvwVYBoGiC5uJ6ZKk77C5tJ6QdiNyMtV3sIh1p9Xf5bmot0slash0v4tvTd9bB4K3SHeNkmxNY1OhesYSK9mOVI0slash0IKLzh60slash0AEmB77UkB1KQwpGOErURCgD1LvWXPrya6IykVJQK8Su9JdSpKdieSdnCU0slash02TR0slash00add0LjLgt0add0SqMxs1PGyb1OL6OKJpY6MG4k5ZTOGtycICcUUCz8sJYgLwV0ow24PZMeJ4rSktAkrx5unhDMJ3SZajWA8LdCGMxvI9uJfqsWlGCo0equals0"
				+ "&timeStamp="+ System.currentTimeMillis();
		HashMap<String, String> requestParams = new HashMap<>();
		requestParams.put("Referer", referer);
		requestParams.put("Host", "www.gapp.gov.cn");
		requestParams.put("X-Requested-With", "XMLHttpRequest");
		requestParams.put("Cookie", "Hm_lvt_f40b5cd7f29dab09f1284d654d4bb944=1544428068,1544428852; Hm_lpvt_f40b5cd7f29dab09f1284d654d4bb944=1544435446; tracker_cookie_35=True; tracker_cookie_datetime_35=2018-12-10%2015%3A53%3A4");
		requestParams.put("Content-Type", "application/x-www-form-urlencoded");
		Document doc = HttpUtils.post(url, requestParams, pars.getBytes());
		Element table = doc.getElementsByTag("table").first();
		Elements trs = table.getElementsByTag("tr");
		for(int i = 0; i < trs.size(); i++ ){
			Element tr = trs.get(i);
			Elements tds = tr.getElementsByTag("td");
			for(int j = 0; j < tds.size(); j+=2){
				data.put(tds.get(j).text(), tds.get(j+1).text());
			}
		}
		return data;
	}
	/**
	 * 还原Unicode编码的字符串
	 * 
	 * @param src
	 * @return String
	 * @author Focus
	 */
	public static String unicode2Chr(String src)
	{
		if( src == null || src.isEmpty() )
		{
			return src;
		}
		String tempStr = "";
		String returnStr = "";

		// 将编码过的字符串进行重排
		for (int i = 0; i < src.length() / 4; i++)
		{
			if (0 == i)
			{
				tempStr = src.substring(4 * i + 2, 4 * i + 4);
				tempStr += src.substring(4 * i, 4 * i + 2);
			}
			else
			{
				tempStr += src.substring(4 * i + 2, 4 * i + 4);
				tempStr += src.substring(4 * i, 4 * i + 2);
			}
		}

		byte[] b = new byte[tempStr.length() / 2];


		try
		{
			// 将重排过的字符串放入byte数组，用于进行转码
			for (int j = 0; j < tempStr.length() / 2; j++)
			{
				String subStr = tempStr.substring(j * 2, j * 2 + 2);
				int b1 = Integer.decode("0x" + subStr).intValue();
				b[j] = (byte) b1;
			}
			// 转码
			returnStr = new String(b, "utf-16");
		}
		catch (UnsupportedEncodingException ex)
		{
			ex.printStackTrace();
		}
		catch(Exception e)
		{
			return src;
		}
		return returnStr;
	}

	public static String chr2Unicode(String src)
	{
		StringBuffer s = new StringBuffer();
		if (src != null && !"".equals(src))
		{
			String hex = "";

			for (int i = 0; i < src.length(); i++)
			{
				hex = Integer.toHexString((int) src.charAt(i));
				if (hex.length() < 4)
				{
					while (hex.length() < 4)
					{
						hex = "0".concat(hex);
					}
				}
				hex = hex.substring(2, 4).concat(hex.substring(0, 2));
				s.append('%');
				s.append(hex.substring(0, 2).toUpperCase());
				s.append('%');
				s.append(hex.substring(2).toUpperCase());
			}
		}
		return s.toString();
	}
}
