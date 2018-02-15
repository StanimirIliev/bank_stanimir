package com.clouway.app

import com.google.appengine.api.datastore.DatastoreServiceFactory
import spark.servlet.SparkApplication

class MainProgram : SparkApplication {
    override fun init() {
        val datastore = DatastoreServiceFactory.getDatastoreService()
        App(datastore)
    }
}