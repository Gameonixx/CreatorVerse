package com.creatorverse.discovery.controller;

import com.creatorverse.discovery.dto.CreatorCardResponse;
import com.creatorverse.discovery.service.CreatorDiscoveryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/discovery/creators")
public class CreatorDiscoveryController {

    private final CreatorDiscoveryService discoveryService;

    public CreatorDiscoveryController(CreatorDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @GetMapping
    public ResponseEntity<Page<CreatorCardResponse>> discoverCreators(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String niche,
            @RequestParam(required = false) Integer minFollowers,
            @RequestParam(required = false) Integer maxFollowers,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "followerCount") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        // Since we are sorting on fields that belong to the User entity (like followerCount), 
        // we might need to handle the Sort properties carefully.
        // With JPA, sorting on a joined entity is done via "user.followerCount"
        String sortProperty = sortBy;
        if (sortBy.equals("followerCount") || sortBy.equals("username") || sortBy.equals("displayName")) {
            sortProperty = "user." + sortBy;
        }

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortProperty);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CreatorCardResponse> response = discoveryService.discoverCreators(search, niche, minFollowers, maxFollowers, pageable);
        return ResponseEntity.ok(response);
    }
}
