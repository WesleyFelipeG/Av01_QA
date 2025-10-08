package com.example.demo.controller;

import com.example.demo.exception.InvalidItemDataException;
import com.example.demo.exception.ItemNotFoundException;
import com.example.demo.model.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
public class ApiController {

    private final List<Item> items = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(items);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new InvalidItemDataException("Nome do item não pode ser vazio ou nulo.");
        }
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            throw new InvalidItemDataException("Descrição do item não pode ser vazia ou nula.");
        }

        item.setId(counter.incrementAndGet());
        items.add(item);
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @GetMapping(value = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return items.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ItemNotFoundException("Item com ID " + id + " não encontrado."));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Item>> searchItemsByName(@RequestParam String name) {
        List<Item> result = items.stream()
                .filter(item -> item.getName() != null && item.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> itemOptional = items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();

        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException("Item com ID " + id + " não encontrado para exclusão.");
        }

        items.remove(itemOptional.get());
        return ResponseEntity.noContent().build();
    }
}
