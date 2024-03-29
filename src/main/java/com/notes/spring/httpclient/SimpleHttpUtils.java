package com.notes.spring.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.core.Logger;


import javax.net.ssl.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

@Slf4j
public class SimpleHttpUtils {

	/**
	 * 默认字符编码
	 */
	public static final String DEFAULT_CHARSET = "utf-8";

	public static final String HTTP_METHOD_POST = "POST";

	public static final String HTTP_METHOD_GET = "GET";
	
	public static final String HTTP_ERROR_MESSAGE = "http_error_message";

	/**
	 * 默认超时设置(20秒)
	 */
	public static final int DEFAULT_READ_TIMEOUT = 20000;
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 10000;


	public static final String HTTP_PREFIX = "http://";

	public static final String HTTPS_PREFIX = "https://";

	//最多只读取5000字节
	public static final int MAX_FETCHSIZE = 5000;
	
	private static TrustManager[] trustAnyManagers = new TrustManager[]{new TrustAnyTrustManager()};
	
	static {
		System.setProperty("sun.net.inetaddr.ttl", "3600");
	}

	public static String httpPost(String url, Map params) {
		return httpRequest(url, params, HTTP_METHOD_POST, DEFAULT_CHARSET, null);
	}

	public static String httpGet(String url, Map params) {
		return httpRequest(url, params, HTTP_METHOD_GET, DEFAULT_CHARSET, null);
	}

	/**
	 * 以建立HttpURLConnection方式发送请求
	 * 
	 * @param targetUrl
	 *            请求地址
	 * @param params
	 *            请求参数
	 * @param method
	 *            请求方式
	 * @param charSet
	 * @return 通讯失败返回null, 否则返回服务端输出
	 */
	public static String httpRequest(String url, Map<String,String> params, String method,
                                     String charSet, Map<String,String> headers) {
		SimpleHttpParam param = new SimpleHttpParam(url);
		if(null != param){
			param.setParameters(params);
		}
		if(null != headers){
			param.setHeaders(headers);
		}
		param.setCharSet(charSet);
		param.setMethod(method);
		SimpleHttpResult result = httpRequest(param);
		if(result==null || !result.isSuccess()){
			return null;
		}else{
			return result.getContent();
		}
	}
	
	/**
	 * 
	 * @param targetUrl
	 * @param queryString
	 * @param sMethod
	 * @param charSet
	 * @param sslVerify
	 * @return
	 */
	public static SimpleHttpResult httpRequest(SimpleHttpParam httpParam) {
		String url = httpParam.getUrl();
		Map<String,Object> parameters = httpParam.getParameters();
		String sMethod = httpParam.getMethod();
		String charSet = httpParam.getCharSet();
		boolean sslVerify = httpParam.isSslVerify();
		int maxResultSize = httpParam.getMaxResultSize();
		Map<String,Object> headers = httpParam.getHeaders();
		int readTimeout = httpParam.getReadTimeout();
		int connectTimeout = httpParam.getConnectTimeout();
		boolean ignoreContentIfUnsuccess = httpParam.isIgnoreContentIfUnsuccess();
		boolean hostnameVerify = httpParam.isHostnameVerify();
		TrustKeyStore trustKeyStore = httpParam.getTrustKeyStore();
		ClientKeyStore clientKeyStore = httpParam.getClientKeyStore();
		
		if (url == null || url.trim().length() == 0) {
			throw new IllegalArgumentException("invalid url : " + url);
		}
		if(maxResultSize<=0){
			throw new IllegalArgumentException("maxResultSize must be positive : "+maxResultSize);
		}
		Charset.forName(charSet);
		HttpURLConnection urlConn = null;
		URL destURL = null;
		
		String baseUrl = url.trim();
		if (!baseUrl.toLowerCase().startsWith(HTTPS_PREFIX) && !baseUrl.toLowerCase().startsWith(HTTP_PREFIX)) {
			baseUrl = HTTP_PREFIX + baseUrl;
		}

		String method = null;
		if (sMethod != null) {
			method = sMethod.toUpperCase();
		}
		if (method == null
				|| !(method.equals(HTTP_METHOD_POST) || method
						.equals(HTTP_METHOD_GET))) {
			throw new IllegalArgumentException("invalid http method : "
					+ method);
		}
		
		int index = baseUrl.indexOf("?");
		if (index>0){
			baseUrl = urlEncode(baseUrl, charSet);
		}else if(index==0){
			throw new IllegalArgumentException("invalid url : " + url);
		}
		
		String queryString = mapToQueryString(parameters, charSet);
		String targetUrl = "";
		if (method.equals(HTTP_METHOD_POST)) {
			targetUrl = baseUrl;
		} else {
			if(index>0){
				targetUrl = baseUrl +"&" + queryString;
			}else{
				targetUrl = baseUrl +"?" + queryString;
			}
		}
		try {
			destURL = new URL(targetUrl);
			urlConn = (HttpURLConnection)destURL.openConnection();
			
			setSSLSocketFactory(urlConn, sslVerify, hostnameVerify, trustKeyStore, clientKeyStore);
			       
			
			boolean hasContentType = false;
			boolean hasUserAgent = false;
			for(String key : headers.keySet()){
				if("Content-Type".equalsIgnoreCase(key)){
					hasContentType = true;
				}
				if("user-agent".equalsIgnoreCase(key)){
					hasUserAgent = true;
				}
			}
			if(!hasContentType){
				headers.put("Content-Type", "application/x-www-form-urlencoded; charset=" + charSet);
			}
			if(!hasUserAgent){
				headers.put("user-agent", "YeepaySystem");
			}
			
			if(headers!=null && !headers.isEmpty()){
				for(Entry<String, Object> entry : headers.entrySet()){
					String key = entry.getKey();
					Object value = entry.getValue();
					List<String> values = makeStringList(value);
					for(String v : values){
						urlConn.addRequestProperty(key, v);
					}
				}
			}
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setAllowUserInteraction(false);
			urlConn.setUseCaches(false);
			urlConn.setRequestMethod(method);
			urlConn.setConnectTimeout(connectTimeout);
			urlConn.setReadTimeout(readTimeout);
			
			
			
			if (method.equals(HTTP_METHOD_POST)) {
				String postData = queryString.length()==0?httpParam.getPostData():queryString;
				if(postData!=null && postData.trim().length()>0){
					OutputStream os = urlConn.getOutputStream();
					OutputStreamWriter osw = new OutputStreamWriter(os, charSet);
					osw.write(postData);
					osw.flush();
					osw.close();
				}
			}

			int responseCode = urlConn.getResponseCode();
			Map<String, List<String>> responseHeaders = urlConn.getHeaderFields();
			String contentType = urlConn.getContentType();
				
			SimpleHttpResult result = new SimpleHttpResult(responseCode);
			result.setHeaders(responseHeaders);
			result.setContentType(contentType);
			
			if(responseCode!=200 && ignoreContentIfUnsuccess){
				return result;
			}
			
			InputStream is = urlConn.getInputStream();
			byte[] temp = new byte[1024];
//			ByteBuffer buffer = ByteBuffer.allocate(maxResultSize);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int readBytes = is.read(temp);
			while(readBytes>0){
//				if(buffer.remaining()>=readBytes){
//					buffer.put(temp, 0, readBytes);
//				}else{
//					buffer.put(temp, 0, buffer.remaining());
//					break;
//				}
				baos.write(temp, 0, readBytes);
				readBytes =  is.read(temp);
			}
			String resultString = new String(baos.toByteArray(), charSet); //new String(buffer.array(), charSet);
			baos.close();
			result.setContent(resultString);
			return result;
		} catch (Exception e) {
			log.warn("connection error : " + e.getMessage());
			return new SimpleHttpResult(e);
		} finally {
			if (urlConn != null) {
				urlConn.disconnect();
			}
		}
	}
	
	public static String urlEncode(String url, String charSet){
		if(url==null || url.trim().length()==0){
			return url;
		}
		int splitIndex = url.indexOf("?");
		if(splitIndex <= 0){
			return url;
		}
		String serviceUrl = url.substring(0, splitIndex);
		String queryString = url.substring(splitIndex+1, url.length());
		String newQueryString = "";
		if(queryString.length()>0){
			String[] nameValues = queryString.split("&");
			for(String nameValue : nameValues){
				int index = nameValue.indexOf("=");
				String pname = null;
				String pvalue = null;
				if(index<0){
					pname = nameValue;
					pvalue = "";
				}
				else{
					pname = nameValue.substring(0, index);
					pvalue = nameValue.substring(index+1, nameValue.length());
					try {
						pvalue = URLEncoder.encode(pvalue, charSet);
					} catch (UnsupportedEncodingException e) {
						throw new IllegalArgumentException("invalid charset : "+charSet);
					}
				}
				newQueryString += pname +"=" + pvalue + "&";
			}
			newQueryString = newQueryString.substring(0, newQueryString.length()-1);
		}
		return serviceUrl + "?" + newQueryString;
	}
	
	public static String mapToQueryString(Map parameters, String charSet) {
		String queryString = "";
		if (parameters!=null && !parameters.isEmpty()) {
			Set<Entry> entrySet = parameters.entrySet();
			for(Entry entry : entrySet){
				try {
					String key = entry.getKey().toString();
					Object value = entry.getValue();
					List values = makeStringList(value);
					for(Object v : values){
						queryString += key + "=" + URLEncoder.encode(v==null?"":v.toString(), charSet) + "&";
					}
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException("invalid charset : " + charSet);
				}
			}
			if (queryString.length() > 0) {
				queryString = queryString.substring(0, queryString.length()-1);
			}
		}
		return queryString;
	}

	public static Map queryStringToMap(String queryString, String charSet) {
		if (queryString == null) {
			throw new IllegalArgumentException("queryString must be specified");
		}

		int index = queryString.indexOf("?");
		if (index > 0) {
			queryString = queryString.substring(index + 1);
		}
		String[] keyValuePairs = queryString.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String keyValue : keyValuePairs) {
			if (keyValue.indexOf("=") == -1) {
				continue;
			}
			String[] args = keyValue.split("=");
			if (args.length == 2) {
				try {
					map.put(args[0], URLDecoder.decode(args[1], charSet));
				} catch (UnsupportedEncodingException e) {
					throw new IllegalArgumentException("invalid charset : " + charSet);
				}
			}
			if (args.length == 1) {
				map.put(args[0], "");
			}
		}
		return map;
	}
	
	private static void setSSLSocketFactory(HttpURLConnection urlConn, boolean sslVerify, boolean hostnameVerify, TrustKeyStore trustCertFactory, ClientKeyStore clientKeyFactory){
		try{
			SSLSocketFactory socketFactory = null;
			if(trustCertFactory!=null || clientKeyFactory!=null || !sslVerify){
				SSLContext sc = SSLContext.getInstance("SSL");
				TrustManager[] trustManagers = null;
				KeyManager[] keyManagers = null;
				if(trustCertFactory!=null){
					trustManagers = trustCertFactory.getTrustManagerFactory().getTrustManagers();
				}
				if(clientKeyFactory!=null){
					keyManagers = clientKeyFactory.getKeyManagerFactory().getKeyManagers();
				}
				if(!sslVerify){
					trustManagers = trustAnyManagers;
					hostnameVerify = false;
				}
				sc.init(keyManagers, trustManagers, new java.security.SecureRandom());
				socketFactory = sc.getSocketFactory();
			}
		
			if(urlConn instanceof HttpsURLConnection){
				HttpsURLConnection httpsUrlCon = (HttpsURLConnection)urlConn;
				if(socketFactory!=null){
					httpsUrlCon.setSSLSocketFactory(socketFactory);
				}
				//设置是否验证hostname
				if(!hostnameVerify){
					httpsUrlCon.setHostnameVerifier(new TrustAnyHostnameVerifier());
				}
			}
			if(urlConn instanceof com.sun.net.ssl.HttpsURLConnection){
				com.sun.net.ssl.HttpsURLConnection httpsUrlCon = (com.sun.net.ssl.HttpsURLConnection)urlConn;
				if(socketFactory!=null){
					httpsUrlCon.setSSLSocketFactory(socketFactory);
				}
				//设置是否验证hostname
				if(!hostnameVerify){
					httpsUrlCon.setHostnameVerifier(new TrustAnyHostnameVerifierOld());
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
	}
	
	private static List<String> makeStringList(Object value){
    	if (value == null) {
    		value = "";
        }
    	List<String> result = new ArrayList<String>();
    	if (value.getClass().isArray()) {
            for (int j = 0; j < Array.getLength(value); j++) {
            	Object obj = Array.get(value, j);
            	result.add(obj!=null?obj.toString():"");
            }
            return result;
        }
    	
        if (value instanceof Iterator) {
            Iterator it = (Iterator)value;
            while(it.hasNext()){
            	Object obj = it.next();
            	result.add(obj!=null?obj.toString():"");
            }
            return result;
        }
        
        if(value instanceof Collection){
        	for(Object obj : (Collection)value){
        		result.add(obj!=null?obj.toString():"");
        	}
        	return result;
        }

        if (value instanceof Enumeration) {
            Enumeration enumeration = (Enumeration) value;
            while (enumeration.hasMoreElements()) {
            	Object obj = enumeration.nextElement();
            	result.add(obj!=null?obj.toString():"");
            }
            return result;
        }
        result.add(value.toString());
        return result;
    }
	
	private static class TrustAnyTrustManager implements X509TrustManager {
	    
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }
    
    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
    
    private static class  TrustAnyHostnameVerifierOld implements com.sun.net.ssl.HostnameVerifier{
		public boolean verify(String arg0, String arg1) {
			return true;
		}
    }
    
    public static ClientKeyStore loadClientKeyStore(String keyStorePath, String keyStorePass, String privateKeyPass){
    	try{
    		return loadClientKeyStore(new FileInputStream(keyStorePath), keyStorePass, privateKeyPass);
    	}catch(Exception e){
    		log.error("loadClientKeyFactory fail : "+e.getMessage(), e);
    		return null;
    	}
    }
    
    public static ClientKeyStore loadClientKeyStore(InputStream keyStoreStream, String keyStorePass, String privateKeyPass){
    	try{
    		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    		KeyStore ks = KeyStore.getInstance("JKS");
    		ks.load(keyStoreStream, keyStorePass.toCharArray());
    		kmf.init(ks, privateKeyPass.toCharArray());
    		return new ClientKeyStore(kmf);
    	}catch(Exception e){
    		log.error("loadClientKeyFactory fail : "+e.getMessage(), e);
    		return null;
    	}
    }
    
    public static TrustKeyStore loadTrustKeyStore(String keyStorePath, String keyStorePass){
    	try{
    		return loadTrustKeyStore(new FileInputStream(keyStorePath), keyStorePass);
    	}catch(Exception e){
    		log.error("loadTrustCertFactory fail : "+e.getMessage(), e);
    		return null;
    	}
    }
    
    public static TrustKeyStore loadTrustKeyStore(InputStream keyStoreStream, String keyStorePass){
    	try{
    		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    		KeyStore ks = KeyStore.getInstance("JKS");
    		ks.load(keyStoreStream, keyStorePass.toCharArray());
    		tmf.init(ks);
    		return new TrustKeyStore(tmf);
    	}catch(Exception e){
    		log.error("loadTrustCertFactory fail : "+e.getMessage(), e);
    		return null;
    	}
    }

    public int test() throws InterruptedException {
    	String str = "http://www.scpta.gov.cn/UserPay/BCallback.asp?p1_MerId=10000216814&r0_Cmd=Buy&r1_Code=1&r2_TrxId=418126905762402E&r3_Amt=100.0&r4_Cur=RMB&r5_Pid=2041&r6_Order=BM2041O243605O2184995OZ&r8_MP=&r9_BType=2&ra_Details=&rb_SplitStatus=SPLITED&rc_SourceFee=&rd_TargetFee=&rq_SourceFee=0.0&rq_TargetFee=0.0&rb_BankId=CEB-NET&ro_BankOrderId=2014595416120928&rp_PayDate=20120928172639&peo_checkInDate=&peo_checkOutDate=&hmac=85b92bb7dc10d9d52189008fe1918f45";
    	for(int i=0; i<10 ;i++ ){
    	String result = httpPost(str, null);
    		System.out.println(result);
    		Thread.currentThread().sleep(1000);
    	}
    	return 0;
    }
	public static void test2() {
		
		SimpleHttpParam http = new SimpleHttpParam("https://www.gdgwpay.com/");
		http.setConnectTimeout(5000);
		http.setMethod("POST");
		SimpleHttpResult request=SimpleHttpUtils.httpRequest(http);
		String html = request.getContent();
		System.out.println(html);
				
	}
}