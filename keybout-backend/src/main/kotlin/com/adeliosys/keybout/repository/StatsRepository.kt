package com.adeliosys.keybout.repository

import com.adeliosys.keybout.model.Stats
import org.springframework.data.mongodb.repository.MongoRepository

interface StatsRepository : MongoRepository<Stats, String>
