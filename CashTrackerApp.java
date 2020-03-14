import java.io.*;
import java.security.cert.Extension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.event.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class CashTrackerApp extends Application {
    public static HBox getHBox(Node ...nodes) {
        HBox temp = new HBox();
        for(Node x: nodes)
            temp.getChildren().add(x);
        temp.setSpacing(10.0);
        temp.setAlignment(Pos.CENTER);
        return temp;
    }

    public static VBox getVBox(Node ...nodes) {
        VBox temp = new VBox();
        for(Node x: nodes)
            temp.getChildren().add(x);
        temp.setSpacing(10.0);
        temp.setAlignment(Pos.CENTER);
        return temp;
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Cash Tracker");

        /* Components of window */
        DatePicker transactionDate = new DatePicker();
        RadioButton credit = new RadioButton("Credit");
        RadioButton debit = new RadioButton("Debit");
        TextField amount = new TextField();
        ChoiceBox<String> transactionMode = new ChoiceBox<String>();
        TextField description = new TextField();
        Button submitButton = new Button("Submit");
        Button clearButton = new Button("Clear");
        Button genDailyReport = new Button("Daily Report");
        Button genMonthlyReport = new Button("Monthly Report");
        Button genYearlyReport = new Button("Yearly Report");
        Text transactionSuccessText = new Text("Transaction successfully noted down");
        Text reportSuccessText = new Text("Report generated successfully");


        /* Layouts of components */
        HBox dateHBox = getHBox(new Text("DATE:"), transactionDate);
        HBox typeHBox = getHBox(new Text("TYPE:"), credit, debit);
        HBox amountHBox = getHBox(new Text("AMOUNT:"), amount);
        HBox modeHBox = getHBox(new Text("MODE:"), transactionMode);
        HBox descriptionHBox = getHBox(new Text("DESCRIPTION:"), description);
        HBox formActionHBox = getHBox(submitButton, clearButton);
        HBox reportActionHBox = getHBox(genDailyReport, genMonthlyReport, genYearlyReport);

        VBox formVBox = getVBox(new Text("NEW ENTRY"), dateHBox, typeHBox, amountHBox, modeHBox, descriptionHBox, formActionHBox);
        VBox reportVBox = getVBox(new Text("GENERATE REPORTS"), reportActionHBox);
        VBox totalVBox = getVBox(formVBox, reportVBox);

        BorderPane complete = new BorderPane();
        complete.setCenter(totalVBox);


        /* Properties of components and layouts */
        amount.setPromptText("Enter amount");
        description.setPromptText("Enter description");
        credit.setSelected(true);
        transactionMode.getItems().addAll("Cash", "Cheque", "Card");
        transactionMode.setValue("Cash");
        totalVBox.setSpacing(50.0);


        /* Event handlers */
        EventHandler<MouseEvent> submit = new EventHandler<MouseEvent>() {
            public String toString(LocalDate date) {
                if(date != null)
                    return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
                else
                    return "";
            }

            public boolean isNumeric(String num) {
                if(num == null)
                    return false;
                try {
                    double number = Double.parseDouble(num);
                }
                catch(NumberFormatException nfe) {
                    return false;
                }
                return true;
            }

            @Override
            public void handle(MouseEvent mouseEvent) {
                String text = toString(transactionDate.getValue()) + " - ";
                if(credit.isSelected())
                    text += "Credit - ";
                else
                    text += "Debit - ";
                if(isNumeric(amount.getText()))
                    text += amount.getText() + " - ";
                text += transactionMode.getValue() + " - " + description.getText() + "\n";

                FileChooser transactionFile = new FileChooser();
                transactionFile.setInitialDirectory(new File("C:\\Users\\chirag\\Documents\\Misc\\Cash files"));
                transactionFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
                try(BufferedWriter bw = new BufferedWriter(new FileWriter(transactionFile.showSaveDialog(stage), true))) {
                    bw.write(text);
                }
                catch(IOException ioe) {
                    ioe.printStackTrace();
                }

                formVBox.getChildren().add(transactionSuccessText);
            }
        };
        submitButton.addEventHandler(MouseEvent.MOUSE_CLICKED, submit);

        EventHandler<MouseEvent> clear = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                transactionDate.setValue(null);
                credit.setSelected(false);
                debit.setSelected(false);
                amount.setText("");
                transactionMode.setValue("Cash");
                description.setText("");
                formVBox.getChildren().removeAll(transactionSuccessText);
            }
        };
        clearButton.addEventHandler(MouseEvent.MOUSE_CLICKED, clear);

        EventHandler<MouseEvent> dailyReport = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Stage reportStage = new Stage();
                reportStage.setTitle("Select date");

                DatePicker reportDate = new DatePicker();
                reportDate.setPromptText("Enter required date for report");
                Button reportGen = new Button("Generate!");

                HBox reportDateHBox = getHBox(new Text("Date:"), reportDate);
                VBox dailyReportVBox = getVBox(reportDateHBox, reportGen);
                BorderPane fullWindow = new BorderPane();
                fullWindow.setCenter(dailyReportVBox);

                EventHandler<MouseEvent> generateDaily = new EventHandler<MouseEvent>() {
                    public String toString(LocalDate date) {
                        if(date != null)
                            return DateTimeFormatter.ofPattern("ddMMyyyy").format(date);
                        else
                            return "";
                    }

                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        String filepath = "C:\\Users\\chirag\\Documents\\Misc\\Cash files\\Report-" + toString(reportDate.getValue()) + ".txt"; // Change filepath according to requirements
                        FileChooser transactionFile = new FileChooser();
                        transactionFile.setInitialDirectory(new File("C:\\Users\\chirag\\Documents\\Misc\\Cash files"));
                        transactionFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
                        try(BufferedReader br = new BufferedReader(new FileReader(transactionFile.showOpenDialog(reportStage)));
                            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
                            String line;
                            double total = 0.0, totalCash = 0.0, totalCheque = 0.0, totalCard = 0.0;
                            while((line = br.readLine()) != null) {
                                String[] comps = line.split(" - ");
                                if(LocalDate.parse(comps[0], DateTimeFormatter.ofPattern("dd/MM/yyyy")).compareTo(reportDate.getValue()) == 0) {
                                    double money = Double.parseDouble(comps[2]);
                                    if(comps[1].equals("Credit")) {
                                        if(comps[3].equals("Cash"))
                                            totalCash += money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque += money;
                                        else
                                            totalCard += money;
                                        total += money;
                                    }
                                    else {
                                        if(comps[3].equals("Cash"))
                                            totalCash -= money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque -= money;
                                        else
                                            totalCard -= money;
                                        total -= money;
                                    }
                                }
                            }
                            bw.write("Net transaction amount = " + total + "\n");
                            bw.write("Net cash trasaction = " + totalCash + "\n");
                            bw.write("Net cheque trasaction = " + totalCheque + "\n");
                            bw.write("Net card trasaction = " + totalCard);
                        }
                        catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                        dailyReportVBox.getChildren().add(reportSuccessText);
                    }
                };
                reportGen.addEventHandler(MouseEvent.MOUSE_CLICKED, generateDaily);

                Scene dailyReport = new Scene(fullWindow, 250, 100);
                reportStage.setScene(dailyReport);
                reportStage.show();
            }
        };
        genDailyReport.addEventHandler(MouseEvent.MOUSE_CLICKED, dailyReport);

        EventHandler<MouseEvent> monthlyReport = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Stage reportStage = new Stage();
                reportStage.setTitle("Select month and year");

                ChoiceBox<String> reportMonth = new ChoiceBox<String>();
                reportMonth.setValue("01");
                reportMonth.getItems().addAll("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12");
                ChoiceBox<String> reportYear = new ChoiceBox<String>();
                int curYear = LocalDate.now().getYear();
                reportYear.getItems().addAll(Integer.toString(curYear), Integer.toString(curYear - 1), Integer.toString(curYear - 2), Integer.toString(curYear - 3), Integer.toString(curYear - 4), Integer.toString(curYear - 5));
                reportYear.setValue(Integer.toString(curYear));
                Button reportGen = new Button("Generate!");

                HBox reportMonthHBox = getHBox(new Text("Month:"), reportMonth);
                HBox reportYearHBox = getHBox(new Text("Year"), reportYear);
                VBox monthlyReportVBox = getVBox(reportMonthHBox, reportYearHBox, reportGen);
                BorderPane fullWindow = new BorderPane();
                fullWindow.setCenter(monthlyReportVBox);

                EventHandler<MouseEvent> generateMonthly = new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        String filepath = "C:\\Users\\chirag\\Documents\\Misc\\Cash files\\Report-" + reportMonth.getValue() + reportYear.getValue() + ".txt"; // Change filepath according to requirements
                        FileChooser transactionFile = new FileChooser();
                        transactionFile.setInitialDirectory(new File("C:\\Users\\chirag\\Documents\\Misc\\Cash files"));
                        transactionFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
                        try(BufferedReader br = new BufferedReader(new FileReader(transactionFile.showOpenDialog(reportStage)));
                            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
                            String line;
                            double total = 0.0, totalCash = 0.0, totalCheque = 0.0, totalCard = 0.0;
                            while((line = br.readLine()) != null) {
                                String[] comps = line.split(" - ");
                                LocalDate curDate = LocalDate.parse(comps[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                if(curDate.getMonthValue() == Integer.parseInt(reportMonth.getValue()) && curDate.getYear() == Integer.parseInt(reportYear.getValue())) {
                                    double money = Double.parseDouble(comps[2]);
                                    if(comps[1].equals("Credit")) {
                                        if(comps[3].equals("Cash"))
                                            totalCash += money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque += money;
                                        else
                                            totalCard += money;
                                        total += money;
                                    }
                                    else {
                                        if(comps[3].equals("Cash"))
                                            totalCash -= money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque -= money;
                                        else
                                            totalCard -= money;
                                        total -= money;
                                    }
                                }
                            }
                            bw.write("Net transaction amount = " + total + "\n");
                            bw.write("Net cash trasaction = " + totalCash + "\n");
                            bw.write("Net cheque trasaction = " + totalCheque + "\n");
                            bw.write("Net card trasaction = " + totalCard);
                        }
                        catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                        monthlyReportVBox.getChildren().add(reportSuccessText);
                    }
                };
                reportGen.addEventHandler(MouseEvent.MOUSE_CLICKED, generateMonthly);

                Scene monthlyReport = new Scene(fullWindow, 300, 150);
                reportStage.setScene(monthlyReport);
                reportStage.show();
            }
        };
        genMonthlyReport.addEventHandler(MouseEvent.MOUSE_CLICKED, monthlyReport);

        EventHandler<MouseEvent> yearlyReport = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Stage reportStage = new Stage();
                reportStage.setTitle("Select year");

                ChoiceBox<String> reportYear = new ChoiceBox<String>();
                int curYear = LocalDate.now().getYear();
                reportYear.getItems().addAll(Integer.toString(curYear), Integer.toString(curYear - 1), Integer.toString(curYear - 2), Integer.toString(curYear - 3), Integer.toString(curYear - 4), Integer.toString(curYear - 5));
                reportYear.setValue(Integer.toString(curYear));
                Button reportGen = new Button("Generate!");

                HBox reportYearHBox = getHBox(new Text("Year"), reportYear);
                VBox yearlyReportVBox = getVBox(reportYearHBox, reportGen);
                BorderPane fullWindow = new BorderPane();
                fullWindow.setCenter(yearlyReportVBox);

                EventHandler<MouseEvent> generateYearly = new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        String filepath = "C:\\Users\\chirag\\Documents\\Misc\\Cash files\\Report-" + reportYear.getValue() + ".txt"; // Change filepath according to requirements
                        FileChooser transactionFile = new FileChooser();
                        transactionFile.setInitialDirectory(new File("C:\\Users\\chirag\\Documents\\Misc\\Cash files"));
                        transactionFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
                        try(BufferedReader br = new BufferedReader(new FileReader(transactionFile.showOpenDialog(reportStage)));
                            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
                            String line;
                            double total = 0.0, totalCash = 0.0, totalCheque = 0.0, totalCard = 0.0;
                            while((line = br.readLine()) != null) {
                                String[] comps = line.split(" - ");
                                LocalDate curDate = LocalDate.parse(comps[0], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                if(curDate.getYear() == Integer.parseInt(reportYear.getValue())) {
                                    double money = Double.parseDouble(comps[2]);
                                    if(comps[1].equals("Credit")) {
                                        if(comps[3].equals("Cash"))
                                            totalCash += money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque += money;
                                        else
                                            totalCard += money;
                                        total += money;
                                    }
                                    else {
                                        if(comps[3].equals("Cash"))
                                            totalCash -= money;
                                        else if(comps[3].equals("Cheque"))
                                            totalCheque -= money;
                                        else
                                            totalCard -= money;
                                        total -= money;
                                    }
                                }
                            }
                            bw.write("Net transaction amount = " + total + "\n");
                            bw.write("Net cash trasaction = " + totalCash + "\n");
                            bw.write("Net cheque trasaction = " + totalCheque + "\n");
                            bw.write("Net card trasaction = " + totalCard);
                        }
                        catch(IOException ioe) {
                            ioe.printStackTrace();
                        }
                        yearlyReportVBox.getChildren().add(reportSuccessText);
                    }
                };
                reportGen.addEventHandler(MouseEvent.MOUSE_CLICKED, generateYearly);

                Scene yearlyReport = new Scene(fullWindow, 250, 100);
                reportStage.setScene(yearlyReport);
                reportStage.show();
            }
        };
        genYearlyReport.addEventHandler(MouseEvent.MOUSE_CLICKED, yearlyReport);


        /* Stage and scene set up */
        Scene scene = new Scene(complete, 300, 400);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
