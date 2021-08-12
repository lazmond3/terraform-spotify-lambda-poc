import * as React from "react";
import {
  useGlobalState,
  useSetGlobalState,
} from "./contexts/GlobalStateContext";

// Propsの型定義
interface IProps {
  name: string;
}

interface IState {
  count: number;
}

export const SubComponent = () => {
  const state = useGlobalState();
  const setGlobalState = useSetGlobalState();
  const handleClick = React.useCallback(() => {
    console.log("クリックされました");

    setGlobalState((s) => ({
      ...s,
      value: s.value + 1,
    }));
  }, [setGlobalState]);

  return (
    <div>
      <h2>count:</h2>
      <div>{state.value}</div>
      <button onClick={handleClick}>Add +1</button>
    </div>
  );
};
