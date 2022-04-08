package com.cavernasedragoes.api.controller;


import com.cavernasedragoes.domain.exception.EntidadeNaoEncontradaException;
import com.cavernasedragoes.domain.model.Classe;
import com.cavernasedragoes.domain.model.Equipamento;
import com.cavernasedragoes.domain.repository.EquipamentoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "api/v1/equipamentos")
@Api(value = "API Equipamentos")

public class EquipamentoController {

    @Autowired
    EquipamentoRepository equipamentoRepository;

    @GetMapping
    @ApiOperation(value=" Retorna uma lista de todos os Equipamentos")
    public List<Equipamento> listar() {
        return equipamentoRepository.findAll();
    }

    @GetMapping("/{equipamentoId}")
    @ApiOperation(value=" Retorna Equipamento especifico pelo id")
    public ResponseEntity<Equipamento> buscar(@PathVariable Long equipamentoId) {
        Optional<Equipamento> equipamento = equipamentoRepository.findById(equipamentoId);

        if (equipamento.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(equipamento.get());
    }

    @PostMapping
    @ApiOperation(value = "Cria Equipamento")
    public ResponseEntity<?> adicionar(@RequestBody Equipamento equipamento) {
        try {
            equipamento = equipamentoRepository.save(equipamento);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(equipamento);
        } catch (EntidadeNaoEncontradaException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
    @PutMapping("/{equipamentoId}")
    @ApiOperation(value= "Atualiza um Equipamento")
    public ResponseEntity<?> atualizar(@PathVariable Long equipamentoId,
                                       @RequestBody Equipamento novoEquipamento){
        try {
            Optional<Equipamento> equipamentoAtual = equipamentoRepository.findById(equipamentoId);

            if (equipamentoAtual.isPresent()) {
                BeanUtils.copyProperties(novoEquipamento, equipamentoAtual.get(),"id");
                equipamentoAtual = Optional.ofNullable(equipamentoRepository.save(equipamentoAtual.get()));
                return ResponseEntity.ok(equipamentoAtual);
            }
            return  ResponseEntity.notFound().build();

        } catch (EntidadeNaoEncontradaException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PatchMapping("/{equipamentoId}")
    @ApiOperation(value="Atualiza um Equipamento parcialmente")
    public  ResponseEntity<?> atualizarParcial(@PathVariable Long equipamentoId,
                                               @RequestBody Map<String, Object> campos) {
        Optional<Equipamento> equipamentoAtual = equipamentoRepository.findById(equipamentoId);

        if (equipamentoAtual.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        merge(campos, equipamentoAtual.get());

        return atualizar(equipamentoId, equipamentoAtual.get());
    }
    private void merge(Map<String, Object> dadosOrigem, Equipamento equipamentoDestino){
        ObjectMapper objectMapper = new ObjectMapper();
        Equipamento equipamentoOrigem = objectMapper.convertValue(dadosOrigem, Equipamento.class);

        dadosOrigem.forEach((nomePropiedade, valorPropriedade) -> {
            Field field = ReflectionUtils.findField(Equipamento.class, nomePropiedade);
            field.setAccessible(true);

            Object novoValor = ReflectionUtils.getField(field, equipamentoOrigem);

            ReflectionUtils.setField(field, equipamentoDestino, novoValor);
        });
    }
    @DeleteMapping("/{equipamentoId}")
    @ApiOperation(value= "Deleta um Equipamento")
    public ResponseEntity<Equipamento> deletarPorId (@PathVariable Long equipamentoId) {
        Optional<Equipamento> equipamento = equipamentoRepository.findById(equipamentoId);

        if (equipamento.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        equipamentoRepository.delete(equipamento.get());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
