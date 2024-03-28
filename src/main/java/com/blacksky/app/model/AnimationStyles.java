package com.blacksky.app.model;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;

public class AnimationStyles {

    public static void scaleEffect(Node node, double duration,int cycleCount,boolean isAutoReverse,
                                double fromX,double fromY,double toX,double toY) {
        ScaleTransition transition = new ScaleTransition(Duration.millis(duration),node);
        transition.setCycleCount(cycleCount);
        transition.setAutoReverse(isAutoReverse);
        transition.setFromX(fromX);
        transition.setFromY(fromY);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.play();
    }

    public static void fadeEffect(Node node, double duration,int cycleCount,boolean isAutoReverse,
                                  double fromValue,double toValue) {
        FadeTransition transition = new FadeTransition(Duration.millis(duration),node);
        transition.setCycleCount(cycleCount);
        transition.setAutoReverse(isAutoReverse);
        transition.setFromValue(fromValue);
        transition.setToValue(toValue);
        transition.play();
    }

    public static void translateEffect(Node node, double translateDuration,int cycleCount,boolean isAutoReverse,
                                       double translateFromX,double translateToX,double translateFromY,double translateToY) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(translateDuration),node);
        translateTransition.setCycleCount(cycleCount);
        translateTransition.setAutoReverse(isAutoReverse);
        translateTransition.setFromX(translateFromX);
        translateTransition.setToX(translateToX);
        translateTransition.setFromY(translateFromY);
        translateTransition.setToY(translateToY);
        translateTransition.play();
    }

    public static void translateAndFadeParallelEffect(Node node, double translateDuration,double fadeDuration,int cycleCount,boolean isAutoReverse,
                                                      double translateFromX,double translateToX,double fadeFromValue,double fadeToValue) {
        VBox parent = (VBox) node;
        ParallelTransition parallelTransition = new ParallelTransition();
//        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(translateDuration),node);
        TranslateTransition translateTransition = null;
        FadeTransition fadeTransition = null;
        for ( int id = 0 ; id < parent.getChildren().size() ; id++ ) {
            translateTransition = new TranslateTransition(Duration.millis(translateDuration),parent.getChildren().get(id));
            translateTransition.setCycleCount(cycleCount);
            translateTransition.setAutoReverse(isAutoReverse);
            translateTransition.setFromX(translateFromX);
            translateTransition.setToX(translateToX);
            parallelTransition.getChildren().add(translateTransition);
            translateDuration += 200;
        }

//        translateTransition = new TranslateTransition(Duration.millis(translateDuration+200),parent);
//        translateTransition.setCycleCount(cycleCount);
//        translateTransition.setAutoReverse(isAutoReverse);
//        translateTransition.setFromX(translateFromX);
//        translateTransition.setToX(translateToX);
//        parallelTransition.getChildren().add(translateTransition);


//        FadeTransition fadeTransition = new FadeTransition(Duration.millis(fadeDuration),node);
//        fadeTransition.setCycleCount(cycleCount);
//        fadeTransition.setAutoReverse(isAutoReverse);
//        fadeTransition.setFromValue(fadeFromValue);
//        fadeTransition.setToValue(fadeToValue);


        for ( int id = 0 ; id < parent.getChildren().size() ; id++ ) {
            fadeTransition = new FadeTransition(Duration.millis(fadeDuration),parent.getChildren().get(id));
            fadeTransition.setCycleCount(cycleCount);
            fadeTransition.setAutoReverse(isAutoReverse);
            fadeTransition.setFromValue(fadeFromValue);
            fadeTransition.setToValue(fadeToValue);
            parallelTransition.getChildren().add(fadeTransition);

            fadeDuration += 200;
        }

//        fadeTransition = new FadeTransition(Duration.millis(2000),parent);
//        fadeTransition.setCycleCount(cycleCount);
//        fadeTransition.setAutoReverse(isAutoReverse);
//        fadeTransition.setFromValue(fadeFromValue);
//        fadeTransition.setToValue(fadeToValue);
//        parallelTransition.getChildren().add(fadeTransition);

        parallelTransition.play();
    }

    private static void sprinkleFlyingEffect(Node node, double duration, int[] sprinkleSize,Color sprinkleColor) {
        ObservableList<Circle> sprinkleList = FXCollections.observableArrayList();
        if ( node instanceof AnchorPane) {
            AnchorPane flyingNode = (AnchorPane) node;
            double layoutX = flyingNode.getScene().getWidth();
            double layoutY = flyingNode.getScene().getHeight();
            double maxSprinkles = ( layoutX > 1500 ) ? 50 : (layoutX > 1000 ) ? 35 : 20;
            double minSprinkles = ( layoutX > 1500 ) ? 40 : (layoutX > 1000 ) ? 25 : 15;
            TranslateTransition translateTransition = null;
            FadeTransition fadeTransition = null;
            ParallelTransition parallelTransition = new ParallelTransition();
            Circle sprinkle = null;

            for ( int id = 0 ; id < getRandomValue((int)minSprinkles,(int)maxSprinkles) ; id++ ) {
                sprinkle = new Circle(getRandomValue(sprinkleSize[0],sprinkleSize[1]));
                sprinkle.setFill(sprinkleColor);
                sprinkle.setLayoutX(getRandomValue(0,(int)layoutX));
                sprinkle.setLayoutY(layoutY+(sprinkle.getRadius())+10);

                translateTransition = new TranslateTransition(Duration.millis(duration),sprinkle);
                translateTransition.setAutoReverse(false);
                translateTransition.setCycleCount(1);
                translateTransition.setFromX(0);
                translateTransition.setFromY(0);
                // how far and how height
                translateTransition.setToX(getRandomValue(-80,180));
                translateTransition.setToY(-getRandomValue(100,150));

                fadeTransition = new FadeTransition(Duration.millis(duration),sprinkle);
                fadeTransition.setAutoReverse(false);
                fadeTransition.setCycleCount(1);
                fadeTransition.setFromValue(1);
                fadeTransition.setToValue(0);

                sprinkleList.add(sprinkle);
                flyingNode.getChildren().add(sprinkle);

                parallelTransition.getChildren().addAll(translateTransition,fadeTransition);
            }
            parallelTransition.play();



        } else {
            System.out.println("Can't play sprinkle effect. [ Reason - Node is not AnchorPane ]");
        }


    }

    public static void sprinkleFlyingEffect(Node node,double duration,int times,int delay,Color sprinkleColor) {
        new Thread( () -> {
            int timesCounter = 0;
            while ( timesCounter < times ) {
                Platform.runLater(() -> sprinkleFlyingEffect(node,duration,new int[]{3,8},sprinkleColor) );
                SystemCommandManipulator.delayTime(delay);
                ++timesCounter;
            }
        }).start();
    }

    public static void sprinkleFlyingEffect(Node node,double duration,int times,int delay,int[] sprinkleSize,Color sprinkleColor) {
        new Thread( () -> {
            int timesCounter = 0;
            while ( timesCounter < times ) {
                Platform.runLater(() -> sprinkleFlyingEffect(node,duration,sprinkleSize,sprinkleColor) );
                SystemCommandManipulator.delayTime(delay);
                ++timesCounter;
            }
        }).start();
    }

    private static double getRandomValue(int min , int max) {
        return new Random().nextInt(max)+min;
    }
}
