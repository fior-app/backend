package app.fior.backend.config

import app.fior.backend.data.convertors.ZonedDateTimeReadConverter
import app.fior.backend.data.convertors.ZonedDateTimeWriteConverter
import org.bson.types.Decimal128
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.mongodb.MongoDbFactory
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import java.math.BigDecimal


@Configuration
class MongoConfiguration {

    @Autowired
    private lateinit var dbFactory: MongoDbFactory

    @Autowired
    private lateinit var reactiveDbFactory: ReactiveMongoDatabaseFactory

    @Autowired
    private lateinit var mongoMappingContext: MongoMappingContext

    @Bean
    @Primary
    fun reactiveMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveDbFactory, mongoMappingConverter())
    }

    @Bean
    @Primary
    fun mongoMappingConverter(): MappingMongoConverter {
        val mappingConverter = MappingMongoConverter(DefaultDbRefResolver(dbFactory), mongoMappingContext)

        mappingConverter.setCustomConversions(customConversions())
        return mappingConverter
    }

    fun customConversions(): CustomConversions {
        return MongoCustomConversions(listOf(ZonedDateTimeReadConverter(), ZonedDateTimeWriteConverter()))
    }

}