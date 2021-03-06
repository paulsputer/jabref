package net.sf.jabref.gui.errorconsole;

import javax.inject.Inject;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import net.sf.jabref.gui.AbstractController;
import net.sf.jabref.gui.ClipBoardManager;
import net.sf.jabref.gui.DialogService;
import net.sf.jabref.gui.IconTheme;
import net.sf.jabref.gui.keyboard.KeyBinding;
import net.sf.jabref.gui.keyboard.KeyBindingPreferences;
import net.sf.jabref.logic.util.BuildInfo;

public class ErrorConsoleController extends AbstractController<ErrorConsoleViewModel> {

    @FXML private Button copyLogButton;
    @FXML private Button createIssueButton;
    @FXML private ListView<LogEventViewModel> messagesListView;
    @FXML private Label descriptionLabel;

    @Inject private DialogService dialogService;
    @Inject private ClipBoardManager clipBoardManager;
    @Inject private BuildInfo buildInfo;
    @Inject private KeyBindingPreferences keyBindingPreferences;

    @FXML
    private void initialize() {
        viewModel = new ErrorConsoleViewModel(dialogService, clipBoardManager, buildInfo);

        messagesListView.setCellFactory(createCellFactory());
        messagesListView.itemsProperty().bind(viewModel.allMessagesDataProperty());
        messagesListView.scrollTo(viewModel.allMessagesDataProperty().getSize() - 1);
        messagesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        viewModel.allMessagesDataProperty().addListener((ListChangeListener<LogEventViewModel>) (change -> {
            int size = viewModel.allMessagesDataProperty().size();
            if (size > 0) {
                messagesListView.scrollTo(size - 1);
            }
        }));
        descriptionLabel.setGraphic(IconTheme.JabRefIcon.CONSOLE.getGraphicNode());
    }

    private Callback<ListView<LogEventViewModel>, ListCell<LogEventViewModel>> createCellFactory() {
        return cell -> new ListCell<LogEventViewModel>() {

            private HBox graphic;
            private Node icon;
            private VBox message;
            private Label heading;
            private Label stacktrace;

            {
                graphic = new HBox(10);
                heading = new Label();
                stacktrace = new Label();
                message = new VBox();
                message.getChildren().setAll(heading, stacktrace);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            public void updateItem(LogEventViewModel event, boolean empty) {
                super.updateItem(event, empty);

                if (event == null || empty) {
                    setGraphic(null);
                } else {
                    icon = event.getIcon().getGraphicNode();
                    heading.setText(event.getDisplayText());
                    heading.getStyleClass().setAll(event.getStyleClass());
                    stacktrace.setText(event.getStackTrace().orElse(""));
                    graphic.getStyleClass().setAll(event.getStyleClass());
                    graphic.getChildren().setAll(icon, message);
                    setGraphic(graphic);
                }
            }
        };
    }

    @FXML
    private void copySelectedLogEntries(KeyEvent event) {
        if (keyBindingPreferences.checkKeyCombinationEquality(KeyBinding.COPY, event)) {
            ObservableList<LogEventViewModel> selectedEntries = messagesListView.getSelectionModel().getSelectedItems();
            viewModel.copyLog(selectedEntries);
        }
    }

    @FXML
    private void copyLog() {
        viewModel.copyLog();
    }

    @FXML
    private void createIssue() {
        viewModel.reportIssue();
    }

    @FXML
    private void closeErrorDialog() {
        getStage().close();
    }
}
