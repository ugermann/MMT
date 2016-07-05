#set(RESOURCES res/vocab.tdx res/corpus.mct res/align.mam)
#set(TEST_SOURCES VocabTests.cpp CorpusTests.cpp TokenIndexTests.cpp BenchmarkTests.cpp)

# maybe put this in some higher level?
find_package(GTest REQUIRED)


#set(RESOURCES res/moses.template.ini)
set(RESOURCES)
set(TEST_SOURCES MosesDecoderTests.cpp)

file(COPY ${RESOURCES} DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/res/)

# ${CMAKE_SOURCE_DIR} == <MMT>/src/Moses/native
set(ENGINE_PATH ${CMAKE_SOURCE_DIR}/../../../engines/default/)  # used in moses.template.ini, must have trailing /
configure_file(res/moses.template.ini ${CMAKE_CURRENT_BINARY_DIR}/res/moses.ini)

foreach(testSrc ${TEST_SOURCES})
    get_filename_component(testName ${testSrc} NAME_WE)
    add_executable(run${testName} ${testSrc})
    target_link_libraries(run${testName} jnimoses gtest gtest_main  tcmalloc profiler  ${Boost_LIBRARIES} ${ZLIB_LIBRARIES} pthread)
endforeach(testSrc)

# ${Boost_LIBRARIES} ${GooglePerftools_LIBRARIES} moses
