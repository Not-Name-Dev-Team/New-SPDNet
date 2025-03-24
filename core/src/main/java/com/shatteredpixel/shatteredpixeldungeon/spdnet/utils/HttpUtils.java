package com.shatteredpixel.shatteredpixeldungeon.spdnet.utils;

import org.apache.http.Consts;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {
	public static String get(String url, List<BasicNameValuePair> list) throws Exception {
		URL urlObj = new URL(url);
		String params = EntityUtils.toString(new UrlEncodedFormEntity(list, Consts.UTF_8));
		if (!params.isEmpty()) url = url + "?" + params;

		HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
		conn.setRequestMethod("GET");
		conn.connect();

		int responsecode = conn.getResponseCode();
		if (responsecode != 200) {
			throw new RuntimeException("HttpResponseCode: " + responsecode);
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), Consts.UTF_8));
			String inputLine;
			StringBuilder content = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			conn.disconnect();
			return content.toString();
		}
	}

	public static String get(String url) throws Exception {
		return get(url, new ArrayList<>());
	}
}
