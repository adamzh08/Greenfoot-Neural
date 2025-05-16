module de.adamyan.antsimulation {
    requires javafx.controls;
    requires javafx.fxml;


    opens de.adamyan.antsimulation to javafx.fxml;
    exports de.adamyan.antsimulation;
}