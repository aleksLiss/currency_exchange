package com.aleks.currency_exchange.filter;

import jakarta.servlet.*;

import java.io.IOException;

public class CustomFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletResponse.setContentType("application/json;encoding=utf-8");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
