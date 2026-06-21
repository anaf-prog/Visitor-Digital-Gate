package com.vigi.gate;

import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.shared.communication.PushMode;

@Configuration
@Push(PushMode.AUTOMATIC)
public class ApplicationViewConfiguration implements AppShellConfigurator {
    
}
