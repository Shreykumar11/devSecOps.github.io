package com.example.JavaController;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
//@RequestMapping(path = "/first")
public class MyFirstJavaController {
	
	@GetMapping("/")
	public String greet() {
		return "Hello, Welcome To My World !!";
	}
	
	@GetMapping("/Add/{num1}/{num2}")
	public String Add(@PathVariable int num1,
					@PathVariable int num2) {
		return ("Addition = " + (num1 + num2));
	}
	
	@GetMapping("/Subtract/{num1}/{num2}")
	public String Subtract(@PathVariable int num1,
					@PathVariable int num2) {
		return ("Subtraction = " + (num1 - num2));
	}
	
	@GetMapping("/Multiply/{num1}/{num2}")
	public String Multiply(@PathVariable int num1,
					@PathVariable int num2) {
		return ("Multiplication = " + (num1 * num2));
	}
	
	@GetMapping("/Divide/{num1}/{num2}")
	public String Divide(@PathVariable int num1,
					@PathVariable int num2) {
		return ("Division = " + (num1 / num2));
	}
}
