package com.adeliosys.keybout.service

import org.springframework.beans.factory.annotation.Value

/**
 * Base class for services that use the database.
 */
abstract class DatabaseAwareService {

    /**
     * Since the same database server is used for data of all application environments, this attribute is used
     * to differentiate them. It contains the environment name such as "dev" or "prod".
     */
    @Value("\${application.environment.name:dev}")
    protected lateinit var environmentName: String
}
