package com.lee.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancelListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
