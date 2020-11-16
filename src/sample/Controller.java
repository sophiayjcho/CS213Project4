package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/* Controller Class loaded by FXML file and handles events generated through user interactions with GUI.
 *Scene Builder is used to connect GUI to Contoller.java.
 * @author Mohamed Moussa, Sophia Cho
 */
public class Controller implements Initializable {

    @FXML
    private ImageView ImageBox;
    @FXML
    private ListView<Extra> toppingsList, selectedToppingsList;
    @FXML
    private ComboBox<Sandwich> LabelOptions;
    @FXML
    private TextField priceLabel, basicIngredients;

    private Order order;
    private Controller2 controller2;

    /**
     * Implements Initializable Abstract method.
     * Initialize method is automatically called by FXML loader when the controller is first invoked.
     * Sets sandwich choices and extra toppings listviews along with default set to chicken.
     *
     * @param url URL used to resolve relative paths for the root object, null if the location is not known
     * @param rb  used to localize the root object, or null if the root object wasn't localized
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ObservableList<Sandwich> sandwichChoices = FXCollections.observableArrayList(new Chicken(), new Beef(), new Fish());
        ObservableList<Extra> extraToppings = FXCollections.observableArrayList(new Extra("Tomato"), new Extra("Onions"),
                new Extra("Extra meat"), new Extra("Lettuce"), new Extra("Extra Cheese"),
                new Extra("Mushrooms"), new Extra("Bacon"), new Extra("Mustard"),
                new Extra("Ketchup"),
                new Extra("Red Peppers"));

        LabelOptions.setItems(sandwichChoices);
        LabelOptions.setValue(sandwichChoices.get(0));
        toppingsList.setItems(extraToppings);
        toppingsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ImageBox.setImage(sandwichChoices.get(0).sandwichImage());
        updatePrice(sandwichChoices.get(0));
        order = new Order();
        basicIngredients.setText(LabelOptions.getSelectionModel().getSelectedItem().basicIngredients());
    }

    /**
     * Event Handler method for Show Orders Button.
     * Instantiates controller2 instance if null and creates a new stage to display Orderlines.
     * Displays an alert if a Orderline Stage is already openned. Stage is hidden and displayed using show and hide stage.
     * IOException is required in signature for the FXMLloader to load parent Node.
     *
     * @param event ActionEvent generated by clicking on the Show Orders Button.
     */
    @FXML
    public void newStage(ActionEvent event) throws IOException {
        if (controller2 != null) { //if secondStage is created, does not create another stage
            if (controller2.visible()) {
                alertWarning("Order window is open", "Please close window before opening another one");
            } else {
                controller2.showStage();
            }
            return;
        }

        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("sample2" + ".fxml"));
        Parent part = loader2.load();
        Scene scene = new Scene(part);

        this.controller2 = loader2.getController();
        this.controller2.receiveController(this);
        Stage primaryStage = new Stage();
        controller2.setStage(primaryStage);
        primaryStage.setTitle("Orders");
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    /**
     * Event Handler Method for Combo Selections.
     * Price and Image are set based on sandwich selected.
     * Toppings Previously selected are set to this sandwich as well.
     *
     * @param event ActionEvent created when comboBox item is selected.
     */
    @FXML
    void selectSandwich(ActionEvent event) {
        Sandwich sandwich = LabelOptions.getSelectionModel().getSelectedItem();
        ImageBox.setImage(sandwich.sandwichImage());
        sandwich.extras.clear();
        ObservableList<Extra> selectedToppings = selectedToppingsList.getItems();
        sandwich.extras.addAll(selectedToppings);
        updatePrice(sandwich);

        basicIngredients.setText(sandwich.basicIngredients());


    }

    /**
     * Event Handler method for Remove Ingredients Button.
     * Shows Alert if there aren't any selected toppings, else removes ingredients and updates price.
     *
     * @param event ActionEvent created when Remove Button is clicked.
     */
    @FXML
    void remove(ActionEvent event) {
        Sandwich selected = currentOrder();
        ObservableList<Extra> selectedToppings = selectedToppingsList.getSelectionModel().getSelectedItems();
        if (!selected.remove(selectedToppings)) {
            alertWarning("No Toppings Selected", "Please select a Topping!");
        } else {
            toppingsList.getItems().addAll(selectedToppings);
            selectedToppingsList.getItems().removeAll(selectedToppings);
            updatePrice(selected);
        }
    }

    /**
     * Event Handler for adding Ingredients.
     * If Toppings selected are  result in more than 6 toppings on a Sandiwch an alert is displayed and program exits.
     * else toppings are placed on the Sandwich and displayed on GUI,price is updated accordingly.
     *
     * @param event ActionEvent generated when add Button is selected.
     */
    @FXML
    void add(ActionEvent event) {
        Sandwich selected = currentOrder();
        ObservableList<Extra> selectedToppings = toppingsList.getSelectionModel().getSelectedItems();
        if (selectedToppings.size() == 0) {
            alertWarning("No Topping Selected", "Please select a Topping");
        }
        if (!selected.add(selectedToppings)) {
            alertWarning("Max Toppings", "You cannot add more than 6 " + "toppings!");

        } else {
            selectedToppingsList.getItems().addAll(selectedToppings);
            toppingsList.getItems().removeAll(selectedToppings);
            updatePrice(selected);
        }
    }

    /**
     * Helper method that returns currently selected Sandwich form ComboBox.
     *
     * @return sandwich instance selected.
     */
    private Sandwich currentOrder() {
        return LabelOptions.getSelectionModel().getSelectedItem();
    }

    /**
     * Helper Method that updatesPrice for priceLabel when ingredients are added or sandwich selected is switched.
     *
     * @param sandwich Sandwich instance selected.
     */
    private void updatePrice(Sandwich sandwich) {
        double price = sandwich.price() + (sandwich.extras.size() * Sandwich.PER_EXTRA);
        priceLabel.setText(String.format("%.2f", price));
    }

    /**
     * Event Handler method for Submit Button.
     * Creates a new orderLine based on sandwich and ingredients selected and adds the orderLine to the order.
     * Updates SecondStage Order Window if it exists.
     *
     * @param event ActionEvent generated when the button is clicked.
     */
    @FXML
    void submit(ActionEvent event) throws Exception {
        Sandwich selected = currentOrder(), newSandwich;

        if (selected instanceof Chicken) {
            newSandwich = new Chicken(selected);
        } else if (selected instanceof Beef) {
            newSandwich = new Beef(selected);
        } else {
            newSandwich = new Fish(selected);
        }

        double price = Double.parseDouble(priceLabel.getText());
        Orderline orderLine = new Orderline(newSandwich, price);

        if (order.add(orderLine)) {
            alertInfo("Order added", "Your order was successfully added.");
        }

        //sets orderlines to second window, updates it
        if (this.controller2 != null) {
            controller2.updateOrderlines();
        }
    }

    /**
     * Getter Method the returns Order Instance.
     * Used by Controller2 to access Orders.
     *
     * @return Order current Order instance
     */
    public Order passOrder() {
        return order;
    }

    /**
     * Helper Method used when Clear Orders Button is pressed.
     * Creates a new Order instance, erasing all of the previous Order Information.
     * Called by Controller2 in the process of clearing Orders.
     */
    public void clearOrder() {
        order = new Order();
    }

    /**
     * Helper Method used to display Alerts.
     *
     * @param Header  Alert Header to display
     * @param content Text to display within Alert
     */
    private void alertWarning(String Header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(Header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Helper Method used to create an Alert instance and display.
     * Information alert is used to notify when each order is submitted.
     *
     * @param Header  Header of the alert
     * @param content content to display in alert
     */
    private void alertInfo(String Header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Message");
        alert.setHeaderText(Header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}