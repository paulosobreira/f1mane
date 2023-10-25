package br.nnpe;

public class HandlerFactory {
	public static void main(String[] args) {
		HandlerFactory t = new HandlerFactory();
		Logger.logar(t.getHandler(new String[] { "/test", "TestServlet",
				"/test2", "TestServlet2" }, "/servlet/TestServlet"));
	}

	public String getHandler(String[] config, String requestUri) {

		for (int i = 0; i < config.length; i++) {
			if (config[i].startsWith("/")) {
				if (config[i].equals(requestUri)) {
					if (i + 1 < config.length) {
						return config[i + 1];
					}
				}
			}
		}
		String[] toks = requestUri.split("/");

		for (int i = 0; i < config.length; i++) {
			if (config[i].startsWith("/")) {
				if (config[i].replaceAll("/", "").startsWith((toks[1]))) {
					if (i + 1 < config.length) {
						return config[i + 1];
					}
				}

			}

		}
		requestUri = "/";
		for (int i = 0; i < config.length; i++) {
			if (config[i].startsWith("/")) {
				if (config[i].equals(requestUri)) {
					if (i + 1 < config.length) {
						return config[i + 1];
					}
				}
			}
		}
		return "dBMEgt8";
	}
}
