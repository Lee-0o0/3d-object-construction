package com.lee.listener;

import com.lee.Main;
import com.lee.Main1;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartListener implements ActionListener {
    private JFrame frame;
    private JComboBox jComboBox;
    private JTextField thresholdField;
    private JTextField accuracyField;
    private JTextField layerHeightField;
    private JTextField smoothField;
    private JTextField verificationThresholdField;

    public StartListener(JFrame frame,JComboBox jComboBox,JTextField thresholdField,JTextField accuracyField,JTextField layerHeightField,JTextField smoothField,JTextField verificationThresholdField){
        this.frame = frame;
        this.jComboBox = jComboBox;
        this.thresholdField = thresholdField;
        this.accuracyField = accuracyField;
        this.layerHeightField = layerHeightField;
        this.smoothField = smoothField;
        this.verificationThresholdField = verificationThresholdField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String name = (String) jComboBox.getSelectedItem();
        double threshold = Double.valueOf(thresholdField.getText());
        double accuracy = Double.valueOf(accuracyField.getText());
        double layerHeight = Double.valueOf(layerHeightField.getText());
        double smoothThreshold = Double.valueOf(smoothField.getText());
        double verificationThreshold = Double.valueOf(verificationThresholdField.getText());

        try{
            Main1.mainFunction(frame,name,layerHeight,threshold,accuracy,smoothThreshold,verificationThreshold);
        }catch (Exception e1){
            e1.printStackTrace();
        }

    }
}
