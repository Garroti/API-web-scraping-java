package com.gabryelmacedo.scrapingbrasileiraoapi.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gabryelmacedo.scrapingbrasileiraoapi.entity.Partida;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Long> {
	
}