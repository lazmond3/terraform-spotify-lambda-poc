import React, {
  Dispatch,
  SetStateAction,
  useContext,
  useEffect,
  useState,
} from "react";
// import { useLiff } from "react-liff";
// import * as liff2 from "@line/liff";
import liff from "@line/liff";
import {
  GlobalState,
  useGlobalState,
  useSetGlobalState,
} from "../contexts/GlobalStateContext";

const decodeJwt = (token: string) => {
  const base64Url = token.split(".")[1];
  const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
  return JSON.parse(decodeURIComponent(escape(window.atob(base64))));
};

interface props {}

export const LineLogin: React.FC<props> = (props) => {
  const state = useGlobalState();
  const setGlobalState = useSetGlobalState();

  useEffect(() => {
    liff
      .init({
        liffId: "1656158895-Rygo23DL",
      })
      .then(() => {
        const idToken = liff.getDecodedIDToken();
        if (idToken == null) {
          //   obj.log(`外部ブラウザです。`);
          setGlobalState((s) => ({
            ...s,
            logText: "外部ブラウザです！" + "\n" + s.logText,
          }));
        } else {
          //   obj.log(`ログインしました！`);
          //   obj.log(`token: ${JSON.stringify(idToken)}`);
          console.log(`ログインしました！`);
          console.log(`token: ${JSON.stringify(idToken)}`);
          setGlobalState((state) => ({
            ...state,
            value: 2,
            loggedIn: true,
            idToken: idToken,
            pathPattern: "auth",
          }));
        }
      })
      .catch((e) => {
        console.log(`ログインでエラーが発生しました！`);
      });
  }, []);

  useEffect(() => {
    console.log(`state が変化しました: ${JSON.stringify(state)}`);
  }, [state]);
  return <></>;
};
