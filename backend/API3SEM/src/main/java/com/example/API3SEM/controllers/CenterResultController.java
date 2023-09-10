package com.example.API3SEM.controllers;



import com.example.API3SEM.resultCenter.CenterResult;
import com.example.API3SEM.resultCenter.CenterResultRepository;
import com.example.API3SEM.resultCenter.CenterResultRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("cr")
public class CenterResultController {
    @Autowired
    private CenterResultRepository repository;

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping
    public CenterResult saveCenterResult(@RequestBody CenterResultRequestDTO data) {
        try {
            CenterResult centerResultData = new CenterResult(data);
            return repository.save(centerResultData);
        } catch (Exception e) {
            CenterResult centerResultData = new CenterResult(data);
            System.out.println(centerResultData.getNome());
            throw new RuntimeException("Não foi possível Cadastrar o centro de resulto, por favor verifique as informações " + e.getMessage());
        }

}}
