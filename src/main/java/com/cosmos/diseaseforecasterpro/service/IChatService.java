package com.cosmos.diseaseforecasterpro.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IChatService {

    String deepSeekChat(HttpServletRequest req, String question);


}
