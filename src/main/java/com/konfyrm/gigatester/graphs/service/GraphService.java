package com.konfyrm.gigatester.graphs.service;

import com.konfyrm.gigatester.graphs.domain.entity.Graph;
import com.konfyrm.gigatester.graphs.repository.GraphCollectionRepository;
import com.konfyrm.gigatester.graphs.repository.GraphRepository;
import com.konfyrm.gigatester.graphs.repository.GraphSessionRepository;
import com.konfyrm.gigatester.questions.service.ImageService;
import com.konfyrm.gigatester.questions.service.impl.R2ImageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import com.konfyrm.gigatester.pins.domain.PinnedEntityType;
import com.konfyrm.gigatester.pins.repository.UserPinRepository;
import com.konfyrm.gigatester.subjects.repository.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class GraphService {

    private final GraphRepository graphRepository;
    private final GraphSessionRepository graphSessionRepository;
    private final SubjectRepository subjectRepository;
    private final UserPinRepository userPinRepository;
    private final ImageService imageService;
    private final GraphCollectionRepository collectionRepository;

    private static final long MAX_IMAGE_BYTES = 8L * 1024 * 1024;

    public GraphService(GraphRepository graphRepository,
                        GraphSessionRepository graphSessionRepository,
                        SubjectRepository subjectRepository,
                        UserPinRepository userPinRepository,
                        @Qualifier(R2ImageService.QUALIFIER) ImageService imageService,
                        GraphCollectionRepository collectionRepository) {
        this.imageService = imageService;
        this.collectionRepository = collectionRepository;
        this.graphRepository = graphRepository;
        this.graphSessionRepository = graphSessionRepository;
        this.subjectRepository = subjectRepository;
        this.userPinRepository = userPinRepository;
    }

    public Graph addGraph(Graph graph) {
        return graphRepository.save(graph);
    }

    /** Stores the picture of an image-labelling activity and returns its public URL. */
    public String uploadImage(MultipartFile file) throws IOException {
        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images can be uploaded.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The image is larger than 8 MB.");
        }
        String original = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String safeName = original.replaceAll("[^A-Za-z0-9._-]", "_");
        return imageService.uploadWithKey("graphs/" + UUID.randomUUID() + "-" + safeName, file);
    }

    public List<Graph> findGraphs() {
        return graphRepository.findAll();
    }

    public Graph findGraph(UUID id) {
        return graphRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void updateGraph(UUID id, Graph update) {
        Graph existing = findGraph(id);
        existing.setName(update.getName());
        existing.setGraphJson(update.getGraphJson());
        graphRepository.save(existing);
    }

    @Transactional
    public void deleteGraph(UUID id) {
        subjectRepository.findByGraphs_Id(id).forEach(s -> s.getGraphs().removeIf(g -> g.getId().equals(id)));
        collectionRepository.findByGraphs_Id(id).forEach(c -> c.getGraphs().removeIf(x -> x.getId().equals(id)));
        graphSessionRepository.deleteByGraph_Id(id);
        userPinRepository.deleteByEntityTypeAndEntityId(PinnedEntityType.GRAPH, id);
        graphRepository.deleteById(id);
    }

}
