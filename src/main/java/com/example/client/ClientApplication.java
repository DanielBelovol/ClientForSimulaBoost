package com.example.client;

import com.example.client.model.ProxyModel;
import com.example.client.model.RequestToVm;
import com.example.client.service.CommandHandler;
import com.example.client.ws.VmWebSocketClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(ClientApplication.class, args);
//		VmWebSocketClient client = context.getBean(VmWebSocketClient.class);
//		client.connect();
		CommandHandler commandHandler = new CommandHandler();
		ProxyModel proxyModel = new ProxyModel("23.95.150.145",6114, "hqyzhmvb","aeuqkkl7vbxc","Europe/Kiev");
		RequestToVm request = new RequestToVm(null,null,proxyModel, null, 1);
		commandHandler.startBrowserAndSearch(request);
	}

}
