package com.patterns.behavioral.observer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventManagementSystem {
    private static final Logger logger = LoggerFactory.getLogger(EventManagementSystem.class);

    public static void main(String[] args) {
        EventManager eventManager = new EventManager();
        
        EmailNotifier emailNotifier = new EmailNotifier();
        SMSNotifier smsNotifier = new SMSNotifier();
        PushNotifier pushNotifier = new PushNotifier();
        
        eventManager.subscribe("conference", emailNotifier);
        eventManager.subscribe("conference", smsNotifier);
        eventManager.subscribe("workshop", pushNotifier);
        
        try {
            eventManager.notify("conference", "Tech Conference 2024 registration is now open!");
            eventManager.notify("workshop", "New Python Workshop scheduled for next week");
            
            eventManager.unsubscribe("conference", smsNotifier);
            logger.info("Unsubscribed SMS notifier from conference events");
            
            eventManager.notify("conference", "Early bird discount ending soon!");
        } catch (Exception e) {
            logger.error("Error in event notification system", e);
        }
    }
}

interface EventListener {
    void update(String eventType, String message);
}

class EventManager {
    private final Map<String, List<EventListener>> listeners = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        logger.info("Subscribed {} to event type: {}", listener.getClass().getSimpleName(), eventType);
    }

    public void unsubscribe(String eventType, EventListener listener) {
        listeners.getOrDefault(eventType, new ArrayList<>()).remove(listener);
        logger.info("Unsubscribed {} from event type: {}", listener.getClass().getSimpleName(), eventType);
    }

    public void notify(String eventType, String message) {
        List<EventListener> eventListeners = listeners.getOrDefault(eventType, new ArrayList<>());
        for (EventListener listener : eventListeners) {
            try {
                listener.update(eventType, message);
            } catch (Exception e) {
                logger.error("Error notifying listener: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
}

class EmailNotifier implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotifier.class);

    @Override
    public void update(String eventType, String message) {
        logger.info("Sending email notification for {} event: {}", eventType, message);
        System.out.printf("📧 Email sent: %s - %s%n", eventType, message);
    }
}

class SMSNotifier implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(SMSNotifier.class);

    @Override
    public void update(String eventType, String message) {
        logger.info("Sending SMS notification for {} event: {}", eventType, message);
        System.out.printf("📱 SMS sent: %s - %s%n", eventType, message);
    }
}

class PushNotifier implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(PushNotifier.class);

    @Override
    public void update(String eventType, String message) {
        logger.info("Sending push notification for {} event: {}", eventType, message);
        System.out.printf("🔔 Push notification sent: %s - %s%n", eventType, message);
    }
}
