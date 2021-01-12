package com.example.handleresolver.controller;


import com.example.handleresolver.annotation.ValidJson;
import com.example.handlersolver1.dto.UpdateTaskRequestDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

	  @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
	  public String updateTask( @ValidJson UpdateTaskRequestDto request) {
	      System.out.println();
		  return "Ok";
	  }

}
