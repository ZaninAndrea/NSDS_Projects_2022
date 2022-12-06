import "./App.css"
import React, { useState, useEffect } from "react"
import {
    Stack,
    Button,
    Card,
    Grid,
    Badge,
    UnstyledButton,
    Group,
    Text,
    Flex,
} from "@mantine/core"
import { IconShoppingCart } from "@tabler/icons"

const statusColor = {
    REQUESTED: "yellow",
    VALIDATED: "green",
    DELIVERED: "blue",
}

function Order({ data }) {
    return (
        <Card>
            <Grid>
                <Grid.Col span="auto" grow={1}>
                    {data.items.join(", ")}{" "}
                </Grid.Col>
                <Grid.Col span={2} grow={0}>
                    <Badge color={statusColor[data.status]}>
                        {data.status.toLowerCase()}
                    </Badge>
                </Grid.Col>
            </Grid>
        </Card>
    )
}

function MainLink({ label, selected, onClick }) {
    return (
        <UnstyledButton
            sx={(theme) => ({
                display: "block",
                width: "100%",
                padding: theme.spacing.xs,
                borderRadius: theme.radius.sm,
                marginBottom: "8px",
                color: theme.black,
                backgroundColor: selected ? theme.colors.gray[2] : "",
                "&:hover": {
                    backgroundColor: selected
                        ? theme.colors.gray[3]
                        : theme.colors.gray[1],
                },
            })}
            onClick={onClick}
        >
            <Group>
                <Text size="sm">{label}</Text>
            </Group>
        </UnstyledButton>
    )
}

class CustomerView extends React.Component {
    state = {
        orders: [],
        items: ["penna", "scotch", "matita", "gomma", "bloc notes", "quaderno"],
        basket: [],
    }

    componentDidMount() {
        this.updateOrders()
        this.fetchInterval = setInterval(this.updateOrders, 5000)
    }

    componentWillUnmount() {
        clearInterval(this.fetchInterval)
    }

    updateOrders = async () => {
        const res = await fetch("http://localhost:8000/orders").then((res) =>
            res.json()
        )
        this.setState({
            orders: res
                .map((order) => {
                    const id = Object.keys(order)[0]
                    const value = order[id]

                    return {
                        ...value,
                        id,
                    }
                })
                .sort((a, b) => b.timestamp - a.timestamp),
        })
    }

    render() {
        return (
            <>
                <Grid sx={() => ({ height: "calc(100vh - 76px)" })}>
                    <Grid.Col
                        span="auto"
                        shrink={1}
                        sx={() => ({ maxWidth: "300px", height: "100%" })}
                    >
                        <Card
                            style={{
                                marginBottom: "16px",
                                width: "300px",
                                height: "100%",
                            }}
                        >
                            <Text
                                style={{
                                    textAlign: "center",
                                    marginBottom: "16px",
                                }}
                            >
                                Order
                            </Text>
                            {this.state.items.map((item) => (
                                <MainLink
                                    label={item}
                                    selected={
                                        this.state.basket.indexOf(item) !== -1
                                    }
                                    onClick={() => {
                                        this.setState(({ basket }) => {
                                            if (basket.indexOf(item) === -1) {
                                                return {
                                                    basket: [...basket, item],
                                                }
                                            } else {
                                                return {
                                                    basket: basket.filter(
                                                        (i) => i !== item
                                                    ),
                                                }
                                            }
                                        })
                                    }}
                                />
                            ))}
                            <Button
                                leftIcon={<IconShoppingCart />}
                                fullWidth
                                style={{ marginTop: "64px" }}
                                disabled={this.state.basket.length === 0}
                                onClick={() => {
                                    fetch("http://localhost:8000/orders", {
                                        method: "POST",
                                        headers: {
                                            "Content-Type": "text/plain",
                                        },
                                        body: this.state.basket.join("\n"),
                                    })
                                    this.setState({
                                        basket: [],
                                    })
                                }}
                            >
                                Buy
                            </Button>
                        </Card>
                    </Grid.Col>
                    <Grid.Col
                        span="auto"
                        grow={1}
                        style={{
                            height: "calc(100% - 16px)",
                            overflow: "auto",
                        }}
                    >
                        <Stack
                            style={{
                                maxWidth: "600px",
                                margin: "auto",
                            }}
                        >
                            {this.state.orders.map((order) => (
                                <Order data={order} />
                            ))}
                        </Stack>
                    </Grid.Col>
                </Grid>
            </>
        )
    }
}

export default CustomerView
