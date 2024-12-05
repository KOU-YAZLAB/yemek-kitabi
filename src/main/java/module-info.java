module com.yazlab.yazlab {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;

    opens com.yazlab to javafx.fxml;
    exports com.yazlab;
}