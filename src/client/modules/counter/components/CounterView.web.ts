import { Component, OnInit } from '@angular/core';
import { Apollo } from 'apollo-angular';
import gql from 'graphql-tag';
import { COUNTER } from '../reducers/actionTypes';
import { counterStore } from '../reducers/index';

@Component({
  selector: 'counter-view',
  templateUrl: './CounterView.html',
  styles: ['section { margin-bottom: 30px; }']
})
export default class CounterView implements OnInit {
  public count: number = 5;
  public reduxCount: number;

  constructor(private apollo: Apollo) {
    this.setReduxCount();
  }

  public ngOnInit(): void {
    // TODO: for debugging purposes only. Will be removed as soon as GraphQL connection is configured.
    this.apollo
      .watchQuery({
        query: gql`
          query CounterQuery {
            counter {
              amount
            }
          }
        `
      })
      .subscribe(({ data }) => {
        // console.log(data);
      });
  }

  public addCount() {
    this.count++;
  }

  public onReduxIncrement() {
    counterStore.dispatch({ type: COUNTER.INCREMENT });
    this.setReduxCount();
  }

  public onReduxIncrease(value: number) {
    counterStore.dispatch({ type: COUNTER.INCREASE, value });
    this.setReduxCount();
  }

  private setReduxCount() {
    this.reduxCount = counterStore.getState().reduxCount || 1;
  }
}

// import React from "react";
// import PropTypes from "prop-types";
// import Helmet from "react-helmet";
// import { Button } from "reactstrap";
// import styled from "styled-components";
// import PageLayout from "../../../app/PageLayout";
//
// const Section = styled.section`margin-bottom: 30px;`;
//
// const CounterView = ({
//   loading,
//   count,
//   addCount,
//   reduxCount,
//   onReduxIncrement
// }) => {
//   const renderMetaData = () => (
//     <Helmet
//       title="Apollo Starter Kit - Counter"
//       meta={[
//         {
//           name: "description",
//           content: "Apollo Fullstack Starter Kit - Counter example page"
//         }
//       ]}
//     />
//   );
//
//   if (loading) {
//     return (
//       <PageLayout>
//         {renderMetaData()}
//         <div className="text-center">Loading...</div>
//       </PageLayout>
//     );
//   } else {
//     return (
//       <PageLayout>
//         {renderMetaData()}
//         <div className="text-center mt-4 mb-4">
//           <Section>
//             <p>
//               Current count, is {count.amount}. This is being stored server-side
//               in the database and using Apollo subscription for real-time
//               updates.
//             </p>
//             <Button id="graphql-button" color="primary" onClick={addCount(1)}>
//               Click to increase count
//             </Button>
//           </Section>
//           <Section>
//             <p>
//               Current reduxCount, is {reduxCount}. This is being stored
//               client-side with Redux.
//             </p>
//             <Button
//               id="redux-button"
//               color="primary"
//               onClick={onReduxIncrement(1)}
//             >
//               Click to increase reduxCount
//             </Button>
//           </Section>
//         </div>
//       </PageLayout>
//     );
//   }
// };
//
// CounterView.propTypes = {
//   loading: PropTypes.bool.isRequired,
//   count: PropTypes.object,
//   addCount: PropTypes.func.isRequired,
//   reduxCount: PropTypes.number.isRequired,
//   onReduxIncrement: PropTypes.func.isRequired
// };
//
// export default CounterView;
