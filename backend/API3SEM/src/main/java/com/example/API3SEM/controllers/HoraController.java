package com.example.API3SEM.controllers;

import com.example.API3SEM.client.ClientRepository;
import com.example.API3SEM.hora.Hora;
import com.example.API3SEM.hora.HoraRepository;
import com.example.API3SEM.hora.HoraRequestDTO;
import com.example.API3SEM.hora.HoraResponseDTO;
import com.example.API3SEM.utills.TipoEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hour")
@CrossOrigin(origins = "*")
public class HoraController {
    
    @Autowired
    private HoraRepository horaRepository;

    @Autowired
    private ClientRepository clientRepository;

    @GetMapping
    public List<HoraResponseDTO> allHours(){
        List<Hora> response = horaRepository.findAll();
        List<HoraResponseDTO> horas;
        horas = response.stream()
                    .map(this::convertToHoraResponseDTO)
                    .collect(Collectors.toList());
        return horas;
    }

    @GetMapping("/{var}/{filtro}") 
    public ResponseEntity<List<Object>> filtredHours(@PathVariable String var, @PathVariable String filtro){
        List<Object> response = new ArrayList<>();
        List<HoraResponseDTO> horas = new ArrayList<>();
        
        List<Hora> horasFromRepository = null;
        if (filtro.equals("matricula")||filtro.equals("codigo_cr")||filtro.equals("cliente")) {

            if (filtro.equals("matricula")) {
                try {
                    if (!horaRepository.findByLancador(var).isEmpty()) {
                        horasFromRepository = horaRepository.findByLancador(var);
                        horas = horasFromRepository.stream()
                                .map(this::convertToHoraResponseDTO)
                                .collect(Collectors.toList());
                    }else{
                        response.add("O usuário fornecido não possui horas lançadas");
                    }
                }catch (Exception e){
                    response.add(e.getMessage());
                }

            } else if (filtro.equals("codigo_cr")) {
                try{
                    if(!horaRepository.findByCodcr(var).isEmpty()) {
                        horasFromRepository = horaRepository.findByCodcr(var);
                        horas = horasFromRepository.stream()
                                .map(this::convertToHoraResponseDTO)
                                .collect(Collectors.toList());
                    }else {
                        response.add("O CR fornecido não possui horas registradas");
                    }
                }catch (Exception e){
                    response.add(e.getMessage());
                }

            } else if (filtro.equals("cliente")) {
                try {
                    if(!horaRepository.findByCnpj(var).isEmpty()) {
                        horasFromRepository = horaRepository.findByCnpj(var);
                        horas = horasFromRepository.stream()
                                .map(this::convertToHoraResponseDTO)
                                .collect(Collectors.toList());
                    }else {
                        response.add("O cliente fornecido não possui horas registradas");
                    }   
                }catch (Exception e){
                    response.add(e.getMessage());
                }
            }
        }
        else {
            String error = "O valor fornecido de filtro '" + filtro + "' não atende a nenhum dos tipos permitidos. Filtro deve ser 'matricula', 'codigo_cr' ou 'cliente'";
            response.add(error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        response.addAll(horas);
        return ResponseEntity.ok(response);          
    }


    @PostMapping("") //2023-12-1-15-15&2023-12-1-15-45  -  yyyy-mm-dd-hh-mm&yyyy-mm-dd-hh-mm
    public ResponseEntity putHour(@RequestBody HoraRequestDTO horaRequestDTO){

        String msg = null;

        try{

            List<Timestamp> hourRange = new ArrayList<>();
            for(String str : Arrays.asList(horaRequestDTO.intervalo().split("&"))){
                hourRange.add(toTimestamp(str.split("-")));
            }
            System.out.println(horaRequestDTO.justificativa_lan());
            
            if(hourRange.get(0).before(hourRange.get(1))&&clientRepository.existsById(horaRequestDTO.cnpj())){
                try{
                Hora hour = new Hora();
                hour.setCodcr(horaRequestDTO.codigo_cr());
                hour.setLancador(horaRequestDTO.matricula_lancador());
                hour.setCnpj(horaRequestDTO.cnpj());
                hour.setData_hora_inicio(hourRange.get(0));
                hour.setData_hora_fim(hourRange.get(1));
                hour.setTipo(TipoEnum.valueOf(horaRequestDTO.tipo().toUpperCase()).name());
                hour.setJustificativa(horaRequestDTO.justificativa_lan());
                hour.setProjeto(horaRequestDTO.projeto());
                hour.setSolicitante(horaRequestDTO.solicitante());
                horaRepository.save(hour);

                msg = "Hora salva com sucesso";
                }catch(Exception e){
                    msg = "A hora fornecida apresenta inconsistências";
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg+"erro: "+e.getMessage());
                }
            }else{
                if(clientRepository.findById(horaRequestDTO.cnpj()).isEmpty()){
                    msg = "O cliente fornecido não esta cadastrado no sistema";    
                }
                if(hourRange.get(0).after(hourRange.get(1))){
                    msg = "O final da hora não pode anteceder seu início, siga o modelo yyyy-mm-dd-hh-mm&yyyy-mm-dd-hh-mm exemplo '2023-12-1-15-15&2023-12-1-15-45'";
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
            }
            
        }catch (DataIntegrityViolationException ex) {
            Throwable rootCause = ex.getRootCause();
            msg = rootCause+"\n";
            
            if (rootCause instanceof java.sql.SQLException) {
                java.sql.SQLException sqlException = (java.sql.SQLException) rootCause;
                String errorMessage = sqlException.getMessage();
            
                if (errorMessage != null && errorMessage.contains("value too long for type")) {
                    msg.concat("O comprimento de um dos dados passados como chave excede o permitido pelo banco");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(msg);
                }
            } else {
                Exception e = (Exception) rootCause;
                msg.concat("erro desconhecido:\n" + e.getMessage());
            }
        }  
        return ResponseEntity.status(HttpStatus.CREATED).body(msg);       
    }
        

    /**
     * converte uma strig no formato "yyyy-mm-dd-hh-mm" para um instancia de Timestamp
     * @param list 
     * @return Timestamp
     */
    private Timestamp toTimestamp(String[] list){
        Timestamp hour;
        String str = list[0]+"-"+list[1]+"-"+list[2]+" "+list[3]+":"+list[4] + ":00";
        hour = Timestamp.valueOf(str);
        return hour;
    }

    private static Integer stringToInteger(String str){
        int retorno = 0;
        if(str != null){
            for (Character character : str.toCharArray()) {
                retorno++;
            }
        }
        return retorno;
    }
    
    private HoraResponseDTO convertToHoraResponseDTO(Hora hora) {
        HoraResponseDTO response = new HoraResponseDTO(
            String.valueOf(hora.getId()),       //id
            hora.getCodcr(),                    // code_cr
            hora.getLancador(),                 // matricula_lancador
            hora.getCnpj(),                     // cnpj
            hora.getData_hora_inicio(),         // data_hora_inicio
            hora.getData_hora_fim(),            // data_hora_fim
            hora.getTipo(),                     // tipo
            hora.getJustificativa(),            // justificativa_lancamento
            hora.getProjeto(),                  // projeto
            hora.getGestor(),                   // gestor
            hora.getJustificativa_negacao(),    // justificativa_negacao
            hora.getStatus_aprovacao(),         // status_aprovacao
            hora.getSolicitante()               // solicitante_lancamento
        );
        
        return response;
    }
    

}