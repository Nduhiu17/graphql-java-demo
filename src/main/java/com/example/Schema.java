package com.example;


import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaCompiler;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Schema {


    @Autowired
    CharacterRepository characterRepository;

    @Autowired
    EpisodeRepository episodeRepository;

    public GraphQLSchema getSchema() {
        try {
            SchemaCompiler schemaCompiler = new SchemaCompiler();
            ClassPathResource classPathResource = new ClassPathResource("schema.graphql");
            TypeDefinitionRegistry compiledSchema = schemaCompiler.compile(classPathResource.getFile());

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(compiledSchema, buildRuntimeWiring());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    DataFetcher<CharacterRepository.Character> characterDataFetcher = environment -> characterRepository.getCharacter(environment.getArgument("id"));
    DataFetcher<List<CharacterRepository.Character>> allCharacters = environment -> characterRepository.getAll();
    DataFetcher<List<EpisodeRepository.Episode>> allEpisodes = environment -> episodeRepository.getAll();

    DataFetcher<String> getSeason = environment -> {
        EpisodeRepository.Episode episode = environment.getSource();
        return episode.season.name();
    };

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("QueryType", typeWiring -> typeWiring
                        .dataFetcher("character", characterDataFetcher)
                        .dataFetcher("characters", allCharacters)
                        .dataFetcher("episodes", allEpisodes)
                ).type("Episode", typeWiring -> typeWiring
                        .dataFetcher("season", getSeason))
                .build();
    }

}
