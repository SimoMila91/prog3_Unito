package com.prog.client.mailclient.controllers.client;

import com.prog.client.mailclient.models.Email;
import com.prog.client.mailclient.models.User;
import com.prog.client.mailclient.utils.Request;
import com.prog.client.mailclient.utils.Response;
import com.prog.client.mailclient.utils.SerializableEmail;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class NewEmailController {

	@FXML
	private TextArea bodyMail;

	@FXML
	private TextField txtSubject;

	@FXML
	private TextField txtTo;

	private boolean correct = false;

	public User model;
	public ClientController controller;


	public void initializeNewController(User model, ClientController controller, String btnClicked) {
		this.controller = controller;
		this.model = model;
		this.model.writeEmail = new Email(createNewId() + 1, model.emailAddressProperty().get());
		handleBtnClicked(btnClicked);
		bodyMail.textProperty().bindBidirectional(this.model.writeEmail.getBody());
		txtSubject.textProperty().bindBidirectional(this.model.writeEmail.getSubject());
		txtTo.textProperty().bindBidirectional(this.model.writeEmail.getReceiver());
	}


	private void handleBtnClicked(String btn) {
		if (btn.equals("reply")) {
			model.writeEmail.setSubject("RE: " + model.selectedEmail.getSubject().get());
			model.writeEmail.setReceiver(model.selectedEmail.getSender().get());
		}

		if (btn.equals("replyAll")) {
			model.writeEmail.setSubject("RE_ALL: " + model.selectedEmail.getSubject().get());
			String[] receivers = model.selectedEmail.getReceiver().get().split(",");
			StringBuilder finalString = new StringBuilder();

			for (String s : receivers) {
				if (!s.equals(model.emailAddressProperty().get())) {
					finalString.append(s).append(",");
				}
			}
			finalString.append(model.selectedEmail.getSender().get());
			System.out.println(finalString);
			model.writeEmail.setReceiver(finalString.toString());
		}

		if (btn.equals("forward")) {
			model.writeEmail.setBody(model.selectedEmail.getBody().get());
			model.writeEmail.setSubject(model.selectedEmail.getSubject().get());
		}
	}
	@FXML
	public void sendBtnClick(ActionEvent event) {
		handleNewEmail("submit", event);
	}


	private void handleNewEmail(String action, ActionEvent event) {

		if (txtTo.getText().isEmpty() && action.equals("submit")) {
			Alert a = new Alert(Alert.AlertType.ERROR, "Field To: is empty. Add a receiver and try again.");
			a.show();
			return;
		}

		if (txtSubject.getText().isEmpty()) {
			model.writeEmail.setSubject("No subject");
		}

		try {
			controller.openConnection();
			if (action.equals("submit")) {
				String[] addresses = txtTo.textProperty().get().split(",");
				for (String s : addresses) {
					if (!validateEmail(s)) {
						Alert a = new Alert(Alert.AlertType.ERROR, "The email " + s + " is not a valid email. Correct the email and try again.");
						a.show();
						return;
					}
					System.out.println(s.trim());
				}

			}
			controller.sendEmail(new Request(action, new SerializableEmail(model.writeEmail)));
			Response res = controller.getServerResponse();
			if (res.isSuccess()) {
				txtTo.textProperty().unbind();
				txtSubject.textProperty().unbind();
				bodyMail.textProperty().unbind();
				correct = true;
			} else {
				Alert a = new Alert(Alert.AlertType.ERROR, res.getMessage());
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			controller.closeConnection();
		}

		if (correct) {
			Platform.runLater(() -> model.sentProperty().add(model.writeEmail));
			Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
			stage.close();
		}
	}

	@FXML
	public void handleClose(ActionEvent event) {
		txtTo.textProperty().unbind();
		txtSubject.textProperty().unbind();
		bodyMail.textProperty().unbind();
		Stage stage = (Stage) ((Node) (event.getSource())).getScene().getWindow();
		stage.close();
	}

	private boolean validateEmail(String email) {
		boolean res;
		String regex = "^[A-Za-z0-9+_.]+@(.+)$";
		res = Pattern.compile(regex).matcher(email.trim()).matches();
		return res;
	}

	private long createNewId() {
		long id = 0;
		SimpleListProperty<Email> emails = new SimpleListProperty<>(FXCollections.observableArrayList());
		emails.addAll(model.inboxProperty());
		emails.addAll(model.sentProperty());
		emails.addAll(model.trashedProperty());
		for (Email email : emails) {
			id = Math.max(email.getIdEmail(), id);
		}
		return id;
	}

}
