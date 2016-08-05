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

TEST(MosesDecoderTests, translate_add) {
  std::unique_ptr<MosesDecoder> decoder(MosesDecoder::createInstance("res/moses.ini"));

  std::string text = "tudsz magyarul ?";
  translation_t translation;

  std::map<std::string, float> ibm;;
  ibm["ibm"] = 1.f;
  std::map<std::string, float> europarl;
  europarl["europarl"] = 1.f;

  translation = decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation before AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("tudsz magyarul ?", translation.text) << "expect translation to consist of OOVs, since training source should not contain these Hungarian words";

  AddSentencePair(decoder.get());

  translation = decoder->translate(text, 0, NULL, 0);
  std::cerr << "Translation after AddSentencePair(): " << translation.text << "\n";
  EXPECT_EQ("si conosce ungherese ?", translation.text) << "expect these target-side words, since they are the only possible translation seen in training";
}


TEST(MosesDecoderTests, convert_bitext) {
  std::unique_ptr<MosesDecoder> decoder(MosesDecoder::createInstance("res/moses.ini"));

  sto::IncrementalBitext *bitext = decoder->GetIncrementalBitext();

  std::string base = "res/MosesDecoderTests";
  sto::remove_all(base);
  sto::create_directory(base);

  bitext->Write(base + "/bitext.");
}
