package org.javamaster.spring.transactional.controller;

import org.javamaster.spring.transactional.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yudong
 * @date 2020/5/14
 */
@RestController
@RequestMapping("/trans")
public class TransactionalController {

    @Autowired
    private AddressService addressService;

    @GetMapping(value = "/transactionOriginalResearch")
    public String transactionOriginalResearch() {
        return addressService.transactionOriginalResearch();
    }

    @GetMapping(value = "/transactionResearch")
    public String transactionResearch() {
        return addressService.transactionResearch();
    }

    @GetMapping(value = "/transactionResearch1")
    public String transactionResearch1() {
        return addressService.transactionResearch1();
    }

}
