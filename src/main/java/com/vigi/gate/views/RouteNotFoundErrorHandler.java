package com.vigi.gate.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.servlet.http.HttpServletResponse;

@AnonymousAllowed
public class RouteNotFoundErrorHandler extends Div implements HasErrorParameter<NotFoundException> {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        // Alihkan (redirect) navigasi secara otomatis ke root view ("")
        event.forwardTo("");
        
        // Mengembalikan status kode HTTP 302 Found / Moved Temporarily untuk redirection
        return HttpServletResponse.SC_MOVED_TEMPORARILY;
    }
    
}
