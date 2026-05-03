package com.huertoapp.service;

import com.huertoapp.dto.request.PlantDetailRequest;
import com.huertoapp.dto.response.PlantDetailResponse;
import com.huertoapp.model.PlantDetail;
import com.huertoapp.repository.PlantDetailRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantDetailService {

    private final PlantDetailRepository repo;

    @PostConstruct
    @Transactional
    public void seedDefaults() {
        if (repo.count() > 0) return;

        List<PlantDetail> defaults = List.of(
            build("Tomate",    "Solanum lycopersicum",  "3-4 veces/semana", "Pulgón, Mosca blanca, Trips",             "https://es.wikipedia.org/wiki/Solanum_lycopersicum"),
            build("Lechuga",   "Lactuca sativa",        "2-3 veces/semana", "Pulgón, Babosas",                         "https://es.wikipedia.org/wiki/Lactuca_sativa"),
            build("Zanahoria", "Daucus carota",         "2 veces/semana",   "Mosca de la zanahoria, Gusano de alambre","https://es.wikipedia.org/wiki/Daucus_carota"),
            build("Pimiento",  "Capsicum annuum",       "3 veces/semana",   "Araña roja, Trips, Pulgón",               "https://es.wikipedia.org/wiki/Capsicum_annuum"),
            build("Pepino",    "Cucumis sativus",       "3-4 veces/semana", "Mosca blanca, Araña roja",                "https://es.wikipedia.org/wiki/Cucumis_sativus"),
            build("Calabacín", "Cucurbita pepo",        "3 veces/semana",   "Pulgón, Oídio",                           "https://es.wikipedia.org/wiki/Cucurbita_pepo"),
            build("Espinacas", "Spinacia oleracea",     "2 veces/semana",   "Pulgón, Mosca minadora",                  "https://es.wikipedia.org/wiki/Spinacia_oleracea"),
            build("Cebolla",   "Allium cepa",           "1-2 veces/semana", "Trips, Mildiu",                           "https://es.wikipedia.org/wiki/Allium_cepa"),
            build("Ajo",       "Allium sativum",        "1 vez/semana",     "Trips, Nematodos",                        "https://es.wikipedia.org/wiki/Allium_sativum"),
            build("Perejil",   "Petroselinum crispum",  "2-3 veces/semana", "Pulgón, Caracoles",                       "https://es.wikipedia.org/wiki/Petroselinum_crispum"),
            build("Albahaca",  "Ocimum basilicum",      "2-3 veces/semana", "Pulgón, Araña roja",                      "https://es.wikipedia.org/wiki/Ocimum_basilicum"),
            build("Fresas",    "Fragaria × ananassa",   "3 veces/semana",   "Araña roja, Botrytis, Babosas",           "https://es.wikipedia.org/wiki/Fragaria_%C3%97_ananassa")
        );

        repo.saveAll(defaults);
    }

    @Transactional(readOnly = true)
    public List<PlantDetailResponse> findAll() {
        return repo.findAllByOrderByPlantNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlantDetailResponse findByPlantName(String plantName) {
        return repo.findByPlantName(plantName)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No hay ficha para la planta: " + plantName));
    }

    @Transactional
    public PlantDetailResponse create(PlantDetailRequest req) {
        if (repo.findByPlantName(req.plantName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una ficha para: " + req.plantName());
        }
        PlantDetail entity = PlantDetail.builder()
                .plantName(req.plantName())
                .latinName(req.latinName())
                .wateringFrequency(req.wateringFrequency())
                .pests(req.pests())
                .wikipediaUrl(req.wikipediaUrl())
                .build();
        return toResponse(repo.save(entity));
    }

    @Transactional
    public PlantDetailResponse update(Long id, PlantDetailRequest req) {
        PlantDetail entity = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ficha no encontrada"));
        entity.setPlantName(req.plantName());
        entity.setLatinName(req.latinName());
        entity.setWateringFrequency(req.wateringFrequency());
        entity.setPests(req.pests());
        entity.setWikipediaUrl(req.wikipediaUrl());
        return toResponse(repo.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ficha no encontrada");
        }
        repo.deleteById(id);
    }

    private PlantDetail build(String name, String latin, String watering, String pests, String wiki) {
        return PlantDetail.builder()
                .plantName(name)
                .latinName(latin)
                .wateringFrequency(watering)
                .pests(pests)
                .wikipediaUrl(wiki)
                .build();
    }

    private PlantDetailResponse toResponse(PlantDetail e) {
        return new PlantDetailResponse(e.getId(), e.getPlantName(), e.getLatinName(),
                e.getWateringFrequency(), e.getPests(), e.getWikipediaUrl());
    }
}
