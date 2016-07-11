#set(RESOURCES res/vocab.tdx res/corpus.mct res/align.mam)
#set(TEST_SOURCES VocabTests.cpp CorpusTests.cpp TokenIndexTests.cpp BenchmarkTests.cpp)

# maybe put this in some higher level?
find_package(GTest)

if(GTest_FOUND)

#set(RESOURCES res/moses.template.ini)
set(RESOURCES)
set(TEST_SOURCES MosesDecoderTests.cpp StoWriteTests.cpp)

file(COPY ${RESOURCES} DESTINATION ${CMAKE_CURRENT_BINARY_DIR}/res/)

get_filename_component(SOURCE_BASE ${CMAKE_SOURCE_DIR} NAME)
if(SOURCE_BASE MATCHES "^vendor$")
    # make tests work in CLion
    # ${CMAKE_SOURCE_DIR} == <MMT>/vendor
    set(ENGINE_PATH ${CMAKE_SOURCE_DIR}/../engines/default/)  # used in moses.template.ini, must have trailing /
else()
    # the usual case
    # ${CMAKE_SOURCE_DIR} == <MMT>/src/Moses/native
    set(ENGINE_PATH ${CMAKE_SOURCE_DIR}/../../../engines/default/)  # used in moses.template.ini, must have trailing /
endif()

configure_file(res/moses.template.ini ${CMAKE_CURRENT_BINARY_DIR}/res/moses.ini)

foreach(testSrc ${TEST_SOURCES})
    get_filename_component(testName ${testSrc} NAME_WE)
    add_executable(run${testName} ${testSrc})
    target_link_libraries(run${testName} jnimoses gtest gtest_main  tcmalloc profiler  ${Boost_LIBRARIES} ${ZLIB_LIBRARIES} pthread)
endforeach(testSrc)

endif(GTest_FOUND)

# ${Boost_LIBRARIES} ${GooglePerftools_LIBRARIES} moses
