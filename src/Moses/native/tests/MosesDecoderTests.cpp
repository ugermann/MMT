/****************************************************
 * Moses - factored phrase-based language decoder   *
 * Copyright (C) 2015 University of Edinburgh       *
 * Licensed under GNU LGPL Version 2.1, see COPYING *
 ****************************************************/

#include <gtest/gtest.h>

#include <iostream>

#include "wrapper/MosesDecoder.h"

using namespace JNIWrapper;

/*
TEST(MosesDecoderTests, create) {
  MosesDecoder *decoder = MosesDecoder::createInstance("res/moses.ini"); // note: can only run this once (static global feature registration!)
}
*/

void AddSentencePair(MosesDecoder *decoder) {
  //                               0           1        2           3      4
  std::vector<std::string> src = {"magyarul", "egesz", "biztosan", "nem", "tudsz"};
  std::vector<std::string> trg = {"sicuramente", "non", "si", "conosce", "ungherese"};
  //                               0              1      2     3          4

  std::vector<std::pair<size_t, size_t>> aln = {{0,4}, {1,0}, {2,0}, {3,1}, {4,2}, {4,3}};

  decoder->AddSentencePair(src, trg, aln, "europarl");
}

TEST(MosesDecoderTests, translate) {
  MosesDecoder *decoder = MosesDecoder::createInstance("res/moses.ini"); // note: can only run this once (static global feature registration!)
  //std::cerr << "hello world" << std::endl;

  std::string text = "magyarul nem tudsz";
  translation_t translation;

  std::map<std::string, float> ibm;;
  ibm["ibm"] = 1.f;
  std::map<std::string, float> europarl;
  europarl["europarl"] = 1.f;

  translation = decoder->translate(text, 0, NULL, 0);
  std::cout << "Translation 1: " << translation.text << "\n";

  AddSentencePair(decoder);

  translation = decoder->translate(text, 0, NULL, 0);
  std::cout << "Translation 2: " << translation.text << "\n";
}
