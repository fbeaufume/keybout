package com.adeliosys.keybout.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Stats (@Id val id:String?, val name:String)
