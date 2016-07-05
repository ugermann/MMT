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

TEST(MosesDecoderTests, translate) {
  MosesDecoder *decoder = MosesDecoder::createInstance("res/moses.ini"); // note: can only run this once (static global feature registration!)
  //std::cerr << "hello world" << std::endl;

  std::string text = "system information support";
  translation_t translation;

  std::map<std::string, float> ibm;;
  ibm["ibm"] = 1.f;
  std::map<std::string, float> europarl;
  europarl["europarl"] = 1.f;

  translation = decoder->translate(text, 0, NULL, 0);
  std::cout << "Translation: " << translation.text << "\n";
}
