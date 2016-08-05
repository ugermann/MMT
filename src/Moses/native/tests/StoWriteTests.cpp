/****************************************************
 * Moses - factored phrase-based language decoder   *
 * Copyright (C) 2015 University of Edinburgh       *
 * Licensed under GNU LGPL Version 2.1, see COPYING *
 ****************************************************/

#include <gtest/gtest.h>

#include <memory>
#include <iostream>

#include "wrapper/MosesDecoder.h"
#include "IncrementalBitext.h"
#include "filesystem.h"

using namespace JNIWrapper;


void AddSentencePair(MosesDecoder *decoder) {
  //                               0           1        2           3      4
  std::vector<std::string> src = {"magyarul", "egesz", "biztosan", "nem", "tudsz"};
  std::vector<std::string> trg = {"sicuramente", "non", "si", "conosce", "ungherese"};
  //                               0              1      2     3          4

  std::vector<std::pair<size_t, size_t>> aln = {{0,4}, {1,0}, {2,0}, {3,1}, {4,2}, {4,3}};

  decoder->AddSentencePair(src, trg, aln, "europarl");
}

/*
 * Test translate() and in-memory incremental Bitext training
 */
TEST(StoWriteTests, translate_add) {
  // Load MosesDecoder with the MMT demo corpus included in git
  std::unique_ptr<MosesDecoder> decoder(MosesDecoder::createInstance("res/moses.ini"));

  std::string text = "tudsz magyarul ?";
  translation_t translation;

  // Translate a sentence with OOVs

  translation = decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation before AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("tudsz magyarul ?", translation.text) << "expect translation to consist of OOVs, since training source should not contain these Hungarian words";

  // Add a training sentence pair with those OOVs

  AddSentencePair(decoder.get());

  // OOVs must now be resolved in a new translation

  translation = decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation after AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("si conosce ungherese ?", translation.text) << "expect these target-side words, since they are the only possible translation seen in training";
}

/*
 * Load legacy Bitext and convert it to v3 (persistent incremental) format
 */
TEST(StoWriteTests, convert_bitext) {
  std::unique_ptr<MosesDecoder> decoder(MosesDecoder::createInstance("res/moses.ini"));

  sto::IncrementalBitext *bitext = decoder->GetIncrementalBitext();

  std::string base = "res/StoWriteTests";
  sto::remove_all(base);
  sto::create_directory(base);

  bitext->Write(base + "/bitext.");

  /////////////

  // on an unrelated note, can we still use the translate() API? (I suspect FFs are broken in subsequent MosesDecoder objects)

  std::string text = "tudsz magyarul ?";
  translation_t translation;

  // Translate a sentence with OOVs

  translation = decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation before AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("tudsz magyarul ?", translation.text) << "expect translation to consist of OOVs, since training source should not contain these Hungarian words";

}

/*
 * Test on-disk persistent bitext.
 */
TEST(StoWriteTests, persistent_add) {
  // Load legacy bitext
  std::unique_ptr<MosesDecoder> decoder(MosesDecoder::createInstance("res/moses.ini"));

  std::string text = "tudsz magyarul ?";
  translation_t translation;

  sto::IncrementalBitext *bitext = decoder->GetIncrementalBitext();

  std::string base = "res/StoWriteTests";
  sto::remove_all(base);
  sto::create_directory(base);

  // Convert legacy bitext to v3 (persistent incremental) format
  bitext->Write(base + "/bitext.");
  sto::copy_file("/home/david/MMT/vendor/../engines/default/models/phrase_tables/model.en-it.lex", base + "/bitext.en-it.lex"); // TODO paths

  decoder.reset(); // destroy decoder on legacy bitext


  std::unique_ptr<MosesDecoder> new_decoder(MosesDecoder::createInstance("res/new_bitext_moses.ini"));

  // Translate a sentence with OOVs

  translation = new_decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation before AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("tudsz magyarul ?", translation.text) << "expect translation to consist of OOVs, since training source should not contain these Hungarian words";

  // Add a training sentence pair with those OOVs

  AddSentencePair(new_decoder.get());

  // OOVs must now be resolved in a new translation

  translation = new_decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation after AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("si conosce ungherese ?", translation.text) << "expect these target-side words, since they are the only possible translation seen in training";

  new_decoder.reset(); // destroy decoder (close DB, etc.pp.)

  new_decoder.reset(MosesDecoder::createInstance("res/new_bitext_moses.ini")); // re-open the appended bitext

  // OOVs must now be resolved in a new translation

  translation = new_decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation after AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("si conosce ungherese ?", translation.text) << "AddSentencePair() should have been persisted on disk";
}
