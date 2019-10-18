package net.minecord.cordexproxy.model.controller.database

import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin

import java.sql.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class MySQL(hostname: String, port: Int, database: String, username: String, password: String, private val plugin: Plugin) {
    private var aliveConnection: Connection? = null
    private val driver: String = "com.mysql.jdbc.Driver"
    private val connectionString: String = ("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?user=" + username + "&autoReconnect=true&useSSL=false"
            + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8")

    private val connection: Connection?
        get() = if (aliveConnection != null) aliveConnection else createConnection()

    val isConnected: Boolean
        get() = aliveConnection != null

    fun createConnection(): Connection? {
        try {
            Class.forName(driver)
            aliveConnection = DriverManager.getConnection(connectionString)
        } catch (e: SQLException) {
            println("Could not connect to Database! because: " + e.message)
        } catch (e: ClassNotFoundException) {
            println("$driver not found!")
        } catch (e: Exception) {
            println(e.message)
        }

        return aliveConnection
    }

    fun close() {
        try {
            if (connection != null) {
                connection!!.close()
            }
        } catch (ex: SQLException) {
            plugin.logger.info(ex.message)
        }

        aliveConnection = null
    }

    fun query(query: String): Result? {
        return preparedQuery(query, null, true)
    }

    fun query(query: String, retry: Boolean): Result? {
        return preparedQuery(query, null, retry)
    }

    @JvmOverloads
    fun preparedQuery(query: String, placeholders: List<String>?, retry: Boolean = true): Result? {
        try {
            var statement: PreparedStatement? = null

            try {
                statement = connection!!.prepareStatement(query)

                var position = 1
                if (placeholders != null) {
                    for (placeholder in placeholders) {
                        statement!!.setString(position, placeholder)
                        position++
                    }
                }

                if (statement!!.execute()) {
                    return Result(statement, statement.resultSet)
                }
            } catch (e: SQLException) {
                val msg = e.message

                logger.severe("Database query error: $msg")

                if (retry && msg!!.contains("_BUSY")) {
                    logger.severe("Retrying query...")
                    plugin.proxy.scheduler.schedule(plugin, { preparedQuery(query, placeholders, true) }, 1, TimeUnit.SECONDS)
                }
            }

            statement?.close()
        } catch (ex: SQLException) {
            plugin.logger.info(ex.message)
        }

        return null
    }

    @JvmOverloads
    fun getInsertedRow(query: String, placeholders: List<String>? = null): Int {
        var id = 0

        val statement: PreparedStatement
        try {
            statement = connection!!.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

            var position = 1
            if (placeholders != null) {
                for (placeholder in placeholders) {
                    statement.setString(position, placeholder)
                    position++
                }
            }

            statement.executeUpdate()

            val rs = statement.generatedKeys
            if (rs.next())
                id = rs.getInt(1)
            rs.close()

            statement.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }

        return id
    }

    inner class Result(private val statement: Statement, val resultSet: ResultSet) {
        fun close() {
            try {
                this.statement.close()
                this.resultSet.close()
            } catch (e: SQLException) {
            }

        }
    }

    companion object {
        private val logger = ProxyServer.getInstance().logger
    }
}
